package io.vertx.ext.web.healthchecks;

import io.vertx.ext.web.Router;

/**
 * Same as {@link HealthCheckTest} but using a health check handler mounter in a sub-router.
 */
public class HealthCheckWithSubRouterTest extends HealthCheckTest {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    Router sub = Router.router(vertx);
    sub.get("/ping*").handler(healthCheckHandler);
    router.route("/prefix/*").subRouter(sub);
  }

  @Override
  protected String prefix() {
    return "/prefix/ping";
  }
}
