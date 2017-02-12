package io.vertx.ext.healthchecks.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.healthchecks.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static io.vertx.ext.healthchecks.Assertions.assertThatCheck;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class DefaultCompositeHealthCheckTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    vertx.close(v -> done.set(v.succeeded()));
    await().untilAtomic(done, is(true));
  }

  @Test
  public void testWithTwoChildrenOneFailing(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    composite.add("A", new DefaultProcedure(vertx, "A", 1000, future -> future.complete(Status.OK())));
    composite.add("B", new DefaultProcedure(vertx, "B", 1000, future -> future.complete(Status.KO())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

  @Test
  public void testWithTwoChildrenOneFailingReverse(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    composite.add("A", new DefaultProcedure(vertx, "A", 1000, future -> future.complete(Status.KO())));
    composite.add("B", new DefaultProcedure(vertx, "B", 1000, future -> future.complete(Status.OK())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

  @Test
  public void testWithTwoChildren(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    composite.add("A", new DefaultProcedure(vertx, "A", 1000, future -> future.complete(Status.OK())));
    composite.add("B", new DefaultProcedure(vertx, "B", 1000, future -> future.complete(Status.OK())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeUp();
      async.complete();
    });
  }

  @Test
  public void testWithTwoChildrenBothFailing(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    composite.add("A", new DefaultProcedure(vertx, "A", 1000, future -> future.complete(Status.KO())));
    composite.add("B", new DefaultProcedure(vertx, "B", 1000, future -> future.complete(Status.KO())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

  @Test
  public void testOneLevelsOfHierarchyWithOneFailing(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    DefaultCompositeProcedure level1 = new DefaultCompositeProcedure();

    composite.add("level1", level1);
    level1.add("B1", new DefaultProcedure(vertx, "B1", 1000, future -> future.complete(Status.KO())));
    level1.add("B2", new DefaultProcedure(vertx, "B2", 1000, future -> future.complete(Status.OK())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

  @Test
  public void testOneLevelsOfHierarchyWithOneFailingReverse(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    DefaultCompositeProcedure level1 = new DefaultCompositeProcedure();

    composite.add("level1", level1);
    level1.add("B1", new DefaultProcedure(vertx, "B1", 1000, future -> future.complete(Status.OK())));
    level1.add("B2", new DefaultProcedure(vertx, "B2", 1000, future -> future.complete(Status.KO())));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

  @Test
  public void testResultWithoutStatusOrOutcome(TestContext tc) {
    DefaultCompositeProcedure composite = new DefaultCompositeProcedure();
    DefaultCompositeProcedure level1 = new DefaultCompositeProcedure();

    composite.add("level1", level1);
    level1.add("B1", new DefaultProcedure(vertx, "B1", 1000, future -> future.complete(Status.OK())));
    level1.add("B2", new DefaultProcedure(vertx, "B2", 1000, future ->
      // This result as down.
      future.complete(new Status().setData(new JsonObject().put("foo", "bar"))
      )));

    Async async = tc.async();

    composite.check(json -> {
      assertThatCheck(json).hasOutcomeDown();
      async.complete();
    });
  }

}
