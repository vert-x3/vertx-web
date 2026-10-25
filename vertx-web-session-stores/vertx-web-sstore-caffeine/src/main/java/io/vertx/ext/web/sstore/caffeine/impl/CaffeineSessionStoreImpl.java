package io.vertx.ext.web.sstore.caffeine.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.internal.CloseableResource;
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

/**
 * @author <a href="mailto:lazarbulic@gmail.com">Lazar Bulic</a>
 */
public class CaffeineSessionStoreImpl implements SessionStore, CaffeineSessionStore {

  /**
   * Default name for the shared Caffeine sessions cache.
   */
  private static final String DEFAULT_SESSION_CACHE_NAME = "vertx-web.caffeine.sessions";


  private CloseableResource<CaffeineCache> resource;
  private VertxContextPRNG random;
  private VertxInternal vertx;

  public CaffeineSessionStoreImpl() {
    // required for the service loader
  }

  private Cache<String, Session> cache() {
    return resource.get().cache;
  }

  @Override
  public Session createSession(long timeout) {
    return new SharedDataSessionImpl(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SharedDataSessionImpl(random, timeout, length);
  }

  static class CaffeineCache implements io.vertx.core.internal.Closeable {

    private final Cache<String, Session> cache;

    public CaffeineCache(Cache<String, Session> cache) {
      this.cache = cache;
    }

    @Override
    public Future<Void> shutdown(Duration timeout) {
      return null;
    }
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {

    ContextInternal ctx = ((VertxInternal) vertx).getOrCreateContext();
    String cacheName = options.getString("cacheName", DEFAULT_SESSION_CACHE_NAME);

    CloseableResource<CaffeineCache> resource = ((VertxInternal) vertx).createSharedResource(
      "__vertx.shared.caffeine.sessions.store",
      cacheName,
      () -> {
        Cache<String, Session> localCaffeineCache = Caffeine.newBuilder()
          .executor(cmd -> ctx.runOnContext(v -> cmd.run()))
          .expireAfter(Expiry.accessing((String key, Session session) ->
            Duration.ofMillis(session.timeout())))
          .build();
        return new CaffeineCache(localCaffeineCache);
      });

    // initialize a secure random
    this.random = VertxContextPRNG.current(vertx);
    this.vertx = (VertxInternal) vertx;
    this.resource = resource;
    return this;
  }

  @Override
  public long retryTimeout() {
    return 0;
  }

  @Override
  public Future<@Nullable Session> get(String id) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    return ctx.succeededFuture(cache().getIfPresent(id));
  }

  @Override
  public Future<Void> delete(String id) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    cache().invalidate(id);
    return ctx.succeededFuture();
  }

  @Override
  public Future<Void> put(Session session) {
    final ContextInternal ctx = vertx.getOrCreateContext();
    final AbstractSession oldSession = (AbstractSession) cache().getIfPresent(session.id());
    final AbstractSession newSession = (AbstractSession) session;

    if (oldSession != null) {
      // there was already some stored data in this case we need to validate versions
      if (oldSession.version() != newSession.version()) {
        return ctx.failedFuture("Session version mismatch");
      }
    } else if (newSession.isPersisted() && !newSession.isRegenerated()) {
      // the session was in the store but is not anymore (e.g.: deleted by a concurrent request), do not resurrect it
      return ctx.failedFuture("Session was deleted from the store");
    }

    newSession.incrementVersion();
    cache().put(session.id(), session);
    return ctx.succeededFuture();
  }

  @Override
  public Future<Void> clear() {
    final ContextInternal ctx = vertx.getOrCreateContext();
    cache().invalidateAll();
    return ctx.succeededFuture();
  }

  @Override
  public Future<Integer> size() {
    final ContextInternal ctx = vertx.getOrCreateContext();
    return ctx.succeededFuture((int) cache().estimatedSize());
  }

  @Override
  public void close() {
    resource.close();
  }
}
