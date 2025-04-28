package io.vertx.ext.web.sstore.caffeine.impl;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.caffeine.CaffeineSessionStore;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:lazarbulic@gmail.com">Lazar Bulic</a>
 */
public class CaffeineSessionStoreImpl implements SessionStore, CaffeineSessionStore {

  /**
   * Default name for map used to store sessions
   */
  private static final String DEFAULT_SESSION_CACHE_NAME = "vertx-web.caffeine.sessions";


  private AsyncCache<String, Session> localCaffeineCache;
  private VertxContextPRNG random;
  private String sessionCacheName;
  private Closeable closeable;

  private VertxInternal vertx;

  public CaffeineSessionStoreImpl() {
    // required for the service loader
  }

  @Override
  public Session createSession(long timeout) {
    return new SharedDataSessionImpl(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SharedDataSessionImpl(random, timeout, length);
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    // initialize a secure random
    this.random = VertxContextPRNG.current(vertx);
    this.vertx = (VertxInternal) vertx;
    this.sessionCacheName = options.getString("cacheName", DEFAULT_SESSION_CACHE_NAME);
    final ContextInternal ctx = ((VertxInternal) vertx).getOrCreateContext();
    CloseFuture closeFuture = new CloseFuture();
    localCaffeineCache = this.vertx.createSharedResource("__vertx.shared.caffeine.sessions.store", sessionCacheName, closeFuture, cf_ -> {
      AsyncCache<String, Session> localCaffeineCache = Caffeine.newBuilder()
        .executor(cmd -> ctx.runOnContext(v -> cmd.run()))
        .expireAfter(Expiry.creating((String key, Session session) ->
          Duration.ofMillis(session.timeout())))
        .buildAsync();
      cf_.add(Promise::complete);
      return localCaffeineCache;
    });
    closeable = closeFuture;
    return this;
  }

  @Override
  public long retryTimeout() {
    return 0;
  }

  @Override
  public Future<@Nullable Session> get(String id) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    CompletableFuture<Session> result = localCaffeineCache.getIfPresent(id);
    if (result != null) {
      return Future.fromCompletionStage(result, ctx);
    } else {
      return ctx.succeededFuture(null);
    }
  }

  @Override
  public Future<Void> delete(String id) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    return ctx.executeBlocking(() -> {
      localCaffeineCache.synchronous().invalidate(id);
      return null;
    });
  }

  @Override
  public Future<Void> put(Session session) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    CompletableFuture<Session> result = localCaffeineCache.getIfPresent(session.id());
    Future<Session> oldSessionResult = result != null ? Future.fromCompletionStage(result, ctx) : ctx.succeededFuture(null);
    return oldSessionResult
      .compose(old -> {
        final AbstractSession oldSession = (AbstractSession) old;
        final AbstractSession newSession = (AbstractSession) session;

        if (oldSession != null) {
          // there was already some stored data in this case we need to validate versions
          if (oldSession.version() != newSession.version()) {
            return ctx.failedFuture("Session version mismatch");
          }
        }

        // we can now safely store the new version
        newSession.incrementVersion();
        localCaffeineCache.put(session.id(), CompletableFuture.completedFuture(session));
        return ctx.succeededFuture();
      });
  }

  @Override
  public Future<Void> clear() {
    final ContextInternal ctx = vertx.getOrCreateContext();
    return ctx.executeBlocking(() -> {
      localCaffeineCache.synchronous().invalidateAll();
      return null;
    });
  }

  @Override
  public Future<Integer> size() {
    final ContextInternal ctx = vertx.getOrCreateContext();
    return ctx.executeBlocking(() -> (int) localCaffeineCache.synchronous().estimatedSize());
  }

  @Override
  public void close() {
    closeable.close(Promise.promise());
  }

}
