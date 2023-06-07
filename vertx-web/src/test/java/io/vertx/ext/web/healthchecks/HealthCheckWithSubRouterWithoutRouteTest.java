package io.vertx.ext.web.healthchecks;

import io.vertx.ext.web.Router;

/**
 * Same as {@link HealthCheckTest} but using a health check handler mounted in a sub-router.
 */
public class HealthCheckWithSubRouterWithoutRouteTest extends HealthCheckTest {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    // Reproducer for https://github.com/vert-x3/vertx-health-check/issues/13
    // This sub-router does not pass a path to the route but handle all GET requests.
    Router subRouter = Router.router(vertx);
    subRouter.get().handler(healthCheckHandler);
    router.route("/no-route/*").subRouter(subRouter);
  }

  @Override
  protected String prefix() {
    return "/no-route";
  }
}
