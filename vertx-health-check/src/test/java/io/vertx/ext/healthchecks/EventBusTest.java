package io.vertx.ext.healthchecks;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.ext.healthchecks.Assertions.assertThat;
import static io.vertx.ext.healthchecks.Assertions.assertThatCheck;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class EventBusTest {

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  private Vertx vertx;
  private HealthChecks healthChecks;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    healthChecks = HealthChecks.create(vertx);

    vertx.eventBus().consumer("health", message -> healthChecks.invoke(message::reply));
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testSuccess(TestContext tc) {
    Async async = tc.async();
    vertx.eventBus().<JsonObject>send("health", "", reply -> {
      assertThat(reply).succeeded().hasContent();
      assertThatCheck(reply.result().body()).isUp();
      async.complete();
    });
  }

  @Test
  @Repeat(10)
  public void testWithFailure(TestContext tc) {
    Async async = tc.async();

    healthChecks.register("my-failing-procedure", future -> future.fail("boom"));

    vertx.eventBus().<JsonObject>send("health", "", reply -> {
      assertThat(reply).succeeded().hasContent();
      assertThatCheck(reply.result().body()).isDown();
      async.complete();
    });
  }

  @Test
  public void testWithStatusFailure(TestContext tc) {
    Async async = tc.async();

    healthChecks.register("my-failing-procedure", future -> future.complete(Status.KO()));

    vertx.eventBus().<JsonObject>send("health", "", reply -> {
      assertThat(reply).succeeded().hasContent();
      assertThatCheck(reply.result().body()).isDown();
      async.complete();
    });
  }

  @Test
  public void testWithStatusSuccess(TestContext tc) {
    Async async = tc.async();

    healthChecks.register("my-procedure", future -> future.complete(Status.OK()));

    vertx.eventBus().<JsonObject>send("health", "", reply -> {
      assertThat(reply).succeeded().hasContent();
      assertThatCheck(reply.result().body()).isUp();
      async.complete();
    });
  }
}
