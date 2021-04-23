package io.vertx.ext.web.client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.cache.CacheManager;
import io.vertx.ext.web.codec.BodyCodec;

public class CachedHttpRequestImpl extends HttpRequestImpl<Buffer> {

  private final CacheManager cacheManager;

  CachedHttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress,
    Boolean ssl, Integer port, String host, String uri, BodyCodec<Buffer> codec,
    WebClientOptions options) {
    super(client, method, serverAddress, ssl, port, host, uri, codec, options);
    this.cacheManager = new CacheManager(options.getCacheAdapter());
  }

  CachedHttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress,
      String protocol, Boolean ssl, Integer port, String host, String uri, BodyCodec<Buffer> codec,
      WebClientOptions options) {

    super(client, method, serverAddress, protocol, ssl, port, host, uri, codec, options);
    this.cacheManager = new CacheManager(options.getCacheAdapter());
  }

  private CachedHttpRequestImpl(CachedHttpRequestImpl other) {
    super(other);
    this.cacheManager = other.cacheManager;
  }

  @Override
  protected void send(String contentType, Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    cacheManager.processRequest(this).onComplete(cacheAR -> {
      if (cacheAR.succeeded()) {
        // Cache hit!
        handler.handle(Future.succeededFuture(cacheAR.result()));
      } else {
        // Cache miss
        HttpContext<Buffer> context = client.createContext(responseAR -> {
          if (responseAR.succeeded()) {
            cacheManager.processResponse(this, responseAR.result()).onComplete(handler);
          } else {
            handler.handle(responseAR);
          }
        });
        context.prepareRequest(this, contentType, body);
      }
    });
  }
}
