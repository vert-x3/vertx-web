package io.vertx.ext.healthchecks;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class CommonHealthChecksTest extends HealthCheckTestBase {

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Test
  @Repeat(10)
  public void testJDBC_OK() {
    JsonObject config = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver");
    JDBCClient client = JDBCClient.createShared(vertx, config);

    registerJDBCProcedure(client);
    await().until(() -> {
      try {
        return get(200) != null;
      } catch (Exception e) {
        return false;
      }
    });
  }

  private void registerJDBCProcedure(JDBCClient client) {
    handler.register("database",
      future -> client.getConnection(connection -> {
        if (connection.failed()) {
          future.fail(connection.cause());
        } else {
          connection.result().close();
          future.complete(Status.OK());
        }
      }));
  }


  @Test
  public void testJDBC_KO() {
    JsonObject config = new JsonObject()
      .put("url", "jdbc:missing:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver");
    JDBCClient client = JDBCClient.createShared(vertx, config);

    registerJDBCProcedure(client);

    // We use 'fail'
    get(500);
  }


  @Test
  public void testServiceAvailability_OK() {
    ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
    AtomicBoolean done = new AtomicBoolean();
    discovery.publish(HttpEndpoint.createRecord("my-service", "localhost"), ar -> {
      done.set(ar.succeeded());
    });
    await().untilAtomic(done, is(true));

    registerServiceProcedure(discovery);
    get(200);
  }

  private void registerServiceProcedure(ServiceDiscovery discovery) {
    handler.register("service",
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

  @Test
  public void testServiceAvailability_KO() {
    ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
    registerServiceProcedure(discovery);
    get(503);
  }

  @Test
  public void testOnEventBus_OK() {
    vertx.eventBus().consumer("health", ar -> {
      ar.reply("pong");
    });

    registerEventBusProcedure();

    get(200);

  }

  private void registerEventBusProcedure() {
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

  @Test
  public void testOnEventBus_KO() {
    vertx.eventBus().consumer("health", ar -> {
      ar.fail(500, "BOOM !");
    });
    registerEventBusProcedure();
    get(503);

  }

  @Test
  public void testOnEventBus_KO_no_receiver() {
    registerEventBusProcedure();
    get(503);

  }


}
