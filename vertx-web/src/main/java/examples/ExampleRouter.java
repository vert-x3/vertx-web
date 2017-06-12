package examples;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RouteRegistration;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by boxfox on 2017-06-12.
 */
@RouteRegistration(path="/example/path", method = HttpMethod.GET)
public class ExampleRouter implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext ctx) {
    ctx.response().setStatusCode(200).end();
    ctx.response().close();
  }
}
