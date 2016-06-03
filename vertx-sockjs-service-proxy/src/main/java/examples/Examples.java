package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void serviceAndSockJS(Vertx vertx) {
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    ProxyHelper.registerService(SomeDatabaseService.class, vertx, service,
        "database-service-address");


    Router router = Router.router(vertx);
    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
        .addInboundPermitted(new PermittedOptions()
            .setAddress("database-service-address"))
        .addOutboundPermitted(new PermittedOptions()
            .setAddress("database-service-address"));

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route("/eventbus/*").handler(ebHandler);

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
