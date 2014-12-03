package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.apex.core.Router;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Examples {

  public void router(Vertx vertx) {
    Router router = Router.router(vertx);
    Router subRouter = Router.router(vertx);

    router.mountSubRouter("/subpath", subRouter);

    subRouter.route("/foo").handler(rc -> {
      rc.response().setStatusMessage(rc.request().path()).end();
    });
  }
}
