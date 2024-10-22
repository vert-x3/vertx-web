package io.vertx.ext.web.it;

import io.vertx.core.*;
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

  public static class MyVerticle extends VerticleBase {
    SessionStore sessionStore;

    public MyVerticle(final SessionStore sessionStore) {
      this.sessionStore = sessionStore;
    }

    public MyVerticle() {
      super();
    }

    @Override
    public Future<?> start() throws Exception {
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
      return vertx.createHttpServer(options)
        .requestHandler(router)
        .listen(8080);
    }
  }
}
