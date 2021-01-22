package io.vertx.ext.web.proxy.handler.impl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ProxyHandlerImpl implements ProxyHandler {

  private final HttpProxy httpProxy;

  public ProxyHandlerImpl(HttpProxy httpProxy) {
    this.httpProxy = httpProxy;
  }

  public ProxyHandlerImpl(HttpProxy httpProxy, int port, String host) {
    this.httpProxy = httpProxy.target(port, host);
  }

  @Override
  public void handle(RoutingContext ctx) {
    httpProxy.handle(ctx.request());
  }
}
