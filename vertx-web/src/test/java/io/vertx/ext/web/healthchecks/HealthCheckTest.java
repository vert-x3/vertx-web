package io.vertx.ext.web.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

public class HealthCheckTest extends HealthCheckTestBase {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    router.get("/health*").handler(healthCheckHandler);
  }

  protected String prefix() {
    return "/health";
  }

  private Future<JsonObject> get(int status) {
    return getCheckResult(prefix(), status);
  }

  private Future<JsonObject> get(String path, int status) {
    return getCheckResult(prefix() + "/" + path, status);
  }

  @Test
  public void testEmptyChecks(TestContext tc) {
    get(204).onComplete(tc.asyncAssertSuccess());
  }

  @Test
  public void testWithEmptySuccessfulCheck(TestContext tc) {
    healthCheckHandler.register("foo", Promise::complete);

    get(200).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "UP");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(1, checks.size());
      JsonObject check = checks.getJsonObject(0);
      tc.assertEquals("foo", check.getString("id"));
      tc.assertEquals("UP", check.getString("status"));
    }));
  }

  @Test
  public void testWithEmptyFailedCheck(TestContext tc) {
    healthCheckHandler.register("foo", promise -> promise.fail("BOOM"));

    get(503).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "DOWN");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(1, checks.size());
      JsonObject check = checks.getJsonObject(0);
      tc.assertEquals("foo", check.getString("id"));
      tc.assertEquals("DOWN", check.getString("status"));
      JsonObject data = check.getJsonObject("data");
      tc.assertNotNull("data");
      tc.assertEquals("BOOM", data.getString("cause"));
    }));
  }

  @Test
  public void testWithExplicitSuccessfulCheck(TestContext tc) {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK()));

    get(200).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "UP");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(1, checks.size());
      JsonObject check = checks.getJsonObject(0);
      tc.assertEquals("bar", check.getString("id"));
      tc.assertEquals("UP", check.getString("status"));
    }));
  }

  @Test
  public void testWithExplicitFailedCheck(TestContext tc) {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.KO()));

    get(503).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "DOWN");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(1, checks.size());
      JsonObject check = checks.getJsonObject(0);
      tc.assertEquals("bar", check.getString("id"));
      tc.assertEquals("DOWN", check.getString("status"));
    }));
  }

  @Test
  public void testWithExplicitSuccessfulCheckAndData(TestContext tc) {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK(new JsonObject()
      .put("availableMemory", "2Mb"))));

    get(200).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "UP");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(1, checks.size());
      JsonObject check = checks.getJsonObject(0);
      tc.assertEquals("bar", check.getString("id"));
      tc.assertEquals("UP", check.getString("status"));
      JsonObject data = check.getJsonObject("data");
      tc.assertNotNull("data");
      tc.assertEquals("2Mb", data.getString("availableMemory"));
    }));
  }

  @Test
  public void testRetrievingALeaf(TestContext tc) {
    healthCheckHandler
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    Async async = tc.async(4);

    get("sub/A", 200).onComplete(tc.asyncAssertSuccess(jsonObject -> {
      tc.assertEquals("UP", jsonObject.getString("status"));
      tc.assertEquals("UP", jsonObject.getString("outcome"));
      async.countDown();
    }));

    get("sub2/c/C2", 503).onComplete(tc.asyncAssertSuccess(jsonObject -> {
      tc.assertEquals("DOWN", jsonObject.getString("status"));
      tc.assertEquals("DOWN", jsonObject.getString("outcome"));
      async.countDown();
    }));

    // Not found
    get("missing", 404).onComplete(tc.asyncAssertSuccess(v -> async.countDown()));
    // Illegal
    get("sub2/c/C1/foo", 400).onComplete(tc.asyncAssertSuccess(v -> async.countDown()));
  }

  @Test
  public void testWithResultHandler(TestContext tc) {
    Function<CheckResult, Future<CheckResult>> resultMapper = cr -> {
      tc.assertTrue(cr.getUp());
      List<CheckResult> checks = cr.getChecks();
      tc.assertEquals(1, checks.size());
      CheckResult child = checks.get(0);
      tc.assertEquals("bar", child.getId());
      tc.assertTrue(child.getUp());
      checks.add(CheckResult.from("new-check", Status.OK()));
      return Future.succeededFuture(cr);
    };
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK()));
    healthCheckHandler.resultMapper(resultMapper);
    get(200).onComplete(tc.asyncAssertSuccess(json -> {
      tc.assertEquals(json.getString("outcome"), "UP");
      JsonArray checks = json.getJsonArray("checks");
      tc.assertNotNull(checks);
      tc.assertEquals(2, checks.size());
      JsonObject first = checks.getJsonObject(0);
      tc.assertEquals("bar", first.getString("id"));
      tc.assertEquals("UP", first.getString("status"));
      JsonObject second = checks.getJsonObject(1);
      tc.assertEquals("new-check", second.getString("id"));
      tc.assertEquals("UP", second.getString("status"));
    }));
  }
}
