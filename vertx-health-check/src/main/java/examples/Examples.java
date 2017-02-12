package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);

    hc.register("my-procedure", future -> future.complete(Status.OK()));
  }

  public void example2(Vertx vertx) {
    HealthCheckHandler healthCheckHandler1 = HealthCheckHandler.create(vertx);
    HealthCheckHandler healthCheckHandler2 = HealthCheckHandler.create(HealthChecks.create(vertx));

    Router router = Router.router(vertx);
    // Populate the router with routes...
    // Register the health check handler
    router.get("/health*").handler(healthCheckHandler1);
    // Or
    router.get("/ping*").handler(healthCheckHandler2);
  }


  public void example2(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Register procedures
    // It can be done after the route registration, or even at runtime
    healthCheckHandler.register("my-procedure-name", future -> {
      // Do the check ....
      // Upon success do
      future.complete(Status.OK());
      // In case of failure do:
      future.complete(Status.KO());
    });

    router.get("/health").handler(healthCheckHandler);
  }


  public void example3(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Register procedures
    // Procedure can be grouped. The group is deduced using a name with "/".
    // Groups can contains other group
    healthCheckHandler.register("a-group/my-procedure-name", future -> {
      //....
    });
    healthCheckHandler.register("a-group/a-second-group/my-second-procedure-name", future -> {
      //....
    });

    router.get("/health").handler(healthCheckHandler);
  }

  public void example4(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Status can provide addition data provided as JSON
    healthCheckHandler.register("my-procedure-name", future -> {
      future.complete(Status.OK(new JsonObject().put("available-memory", "2mb")));
    });

    healthCheckHandler.register("my-second-procedure-name", future -> {
      future.complete(Status.KO(new JsonObject().put("load", 99)));
    });

    router.get("/health").handler(healthCheckHandler);
  }

  public void jdbc(JDBCClient jdbcClient, HealthCheckHandler handler) {
    handler.register("database",
      future -> jdbcClient.getConnection(connection -> {
        if (connection.failed()) {
          future.fail(connection.cause());
        } else {
          connection.result().close();
          future.complete(Status.OK());
        }
      }));
  }

  public void service(ServiceDiscovery discovery, HealthCheckHandler handler) {
    handler.register("my-service",
      future -> HttpEndpoint.getClient(discovery,
        (rec) -> "my-service".equals(rec.getName()),
        client -> {
          if (client.failed()) {
            future.fail(client.cause());
          } else {
            client.result().close();
            future.complete(Status.OK());
          }
        }));
  }

  public void eventbus(Vertx vertx, HealthCheckHandler handler) {
    handler.register("receiver",
      future ->
        vertx.eventBus().send("health", "ping", response -> {
          if (response.succeeded()) {
            future.complete(Status.OK());
          } else {
            future.complete(Status.KO());
          }
        })
    );
  }

  public void publishOnEventBus(Vertx vertx, HealthChecks healthChecks) {
    vertx.eventBus().consumer("health",
      message -> healthChecks.invoke(message::reply));
  }

}
