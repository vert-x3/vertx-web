package io.vertx.ext.web.it;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class SessionHandlerRegression {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    LocalSessionStore store = LocalSessionStore.create(vertx);
    // vertx.deployVerticle(() -> new MyVerticle(), new DeploymentOptions().setInstances(1)); // Works fine
    vertx.deployVerticle(() -> new MyVerticle(store), new DeploymentOptions().setInstances(1)); // Fails with IllegalStateException
  }

  public static class MyVerticle extends AbstractVerticle {
    SessionStore sessionStore;

    public MyVerticle(final SessionStore sessionStore) {
      this.sessionStore = sessionStore;
    }

    public MyVerticle() {
      super();
    }

    public void start(final Promise<Void> startPromise) {
      if (sessionStore == null) {
        sessionStore = LocalSessionStore.create(vertx);
      }
      Router router = Router.router(vertx);
      router.route().handler(SessionHandler.create(sessionStore));
      router.route().handler(context -> {
        try {
          Thread.sleep(1000); // that's a lot, but it's for example purpose...
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        context.request()
          .endHandler(unused -> context.response()
            .end("hello"));
      });
      HttpServerOptions options = new HttpServerOptions();
      vertx.createHttpServer(options)
        .requestHandler(router)
        .listen(8080, ar -> startPromise.complete());
    }
  }
}
