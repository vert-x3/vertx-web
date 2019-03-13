package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.webauthn.WebAuthN;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.WebAuthNHandlerImpl;

@VertxGen
public interface WebAuthNHandler extends Handler<RoutingContext> {

  static WebAuthNHandler create(WebAuthN webAuthN, String origin) {
    return new WebAuthNHandlerImpl(webAuthN, origin);
  }

  Handler<RoutingContext> loginHandler();

  Handler<RoutingContext> registerHandler();
}
