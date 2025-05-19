package io.vertx.ext.web.proxy.handler.impl;

import io.vertx.core.VertxException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ProxyHandlerImpl implements ProxyHandler, PlatformHandler {

  private static final Throwable BH_FAILURE;

  static {
    String msg = "A " +
                 BodyHandler.class.getSimpleName() +
                 " has been executed before the " +
                 ProxyHandler.class.getSimpleName() +
                 ". They are not compatible, please update your router setup.";
    BH_FAILURE = new VertxException(msg, true);
  }

  private final HttpProxy httpProxy;

  public ProxyHandlerImpl(HttpProxy httpProxy) {
    this.httpProxy = httpProxy;
  }

  public ProxyHandlerImpl(HttpProxy httpProxy, int port, String host) {
    this.httpProxy = httpProxy.origin(port, host);
  }

  @Override
  public void handle(RoutingContext rc) {
    if (rc.body().available()) {
      rc.fail(500, BH_FAILURE);
      return;
    }
    httpProxy.handle(rc.request());
  }
}
