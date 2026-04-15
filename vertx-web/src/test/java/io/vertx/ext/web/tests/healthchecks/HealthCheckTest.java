package io.vertx.ext.web.tests.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class HealthCheckTest extends HealthCheckTestBase {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    router.get("/health*").handler(healthCheckHandler);
  }

  protected String prefix() {
    return "/health";
  }

  private JsonObject get(int status) {
    return getCheckResult(prefix(), status);
  }

  private JsonObject get(String path, int status) {
    return getCheckResult(prefix() + "/" + path, status);
  }

  @Test
  public void testEmptyChecks() {
    get(204);
  }

  @Test
  public void testWithEmptySuccessfulCheck() {
    healthCheckHandler.register("foo", Promise::complete);

    JsonObject json = get(200);
    assertEquals("UP", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(1, checks.size());
    JsonObject check = checks.getJsonObject(0);
    assertEquals("foo", check.getString("id"));
    assertEquals("UP", check.getString("status"));
  }

  @Test
  public void testWithEmptyFailedCheck() {
    healthCheckHandler.register("foo", promise -> promise.fail("BOOM"));

    JsonObject json = get(503);
    assertEquals("DOWN", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(1, checks.size());
    JsonObject check = checks.getJsonObject(0);
    assertEquals("foo", check.getString("id"));
    assertEquals("DOWN", check.getString("status"));
    JsonObject data = check.getJsonObject("data");
    assertNotNull(data);
    assertEquals("BOOM", data.getString("cause"));
  }

  @Test
  public void testWithExplicitSuccessfulCheck() {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK()));

    JsonObject json = get(200);
    assertEquals("UP", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(1, checks.size());
    JsonObject check = checks.getJsonObject(0);
    assertEquals("bar", check.getString("id"));
    assertEquals("UP", check.getString("status"));
  }

  @Test
  public void testWithExplicitFailedCheck() {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.KO()));

    JsonObject json = get(503);
    assertEquals("DOWN", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(1, checks.size());
    JsonObject check = checks.getJsonObject(0);
    assertEquals("bar", check.getString("id"));
    assertEquals("DOWN", check.getString("status"));
  }

  @Test
  public void testWithExplicitSuccessfulCheckAndData() {
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK(new JsonObject()
      .put("availableMemory", "2Mb"))));

    JsonObject json = get(200);
    assertEquals("UP", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(1, checks.size());
    JsonObject check = checks.getJsonObject(0);
    assertEquals("bar", check.getString("id"));
    assertEquals("UP", check.getString("status"));
    JsonObject data = check.getJsonObject("data");
    assertNotNull(data);
    assertEquals("2Mb", data.getString("availableMemory"));
  }

  @Test
  public void testRetrievingALeaf() {
    healthCheckHandler
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    JsonObject jsonA = get("sub/A", 200);
    assertEquals("UP", jsonA.getString("status"));
    assertEquals("UP", jsonA.getString("outcome"));

    JsonObject jsonC2 = get("sub2/c/C2", 503);
    assertEquals("DOWN", jsonC2.getString("status"));
    assertEquals("DOWN", jsonC2.getString("outcome"));

    // Not found
    get("missing", 404);
    // Illegal
    get("sub2/c/C1/foo", 400);
  }

  @Test
  public void testWithResultHandler() {
    Function<CheckResult, Future<CheckResult>> resultMapper = cr -> {
      assertTrue(cr.getUp());
      List<CheckResult> checks = cr.getChecks();
      assertEquals(1, checks.size());
      CheckResult child = checks.get(0);
      assertEquals("bar", child.getId());
      assertTrue(child.getUp());
      checks.add(CheckResult.from("new-check", Status.OK()));
      return Future.succeededFuture(cr);
    };
    healthCheckHandler.register("bar", promise -> promise.complete(Status.OK()));
    healthCheckHandler.resultMapper(resultMapper);

    JsonObject json = get(200);
    assertEquals("UP", json.getString("outcome"));
    JsonArray checks = json.getJsonArray("checks");
    assertNotNull(checks);
    assertEquals(2, checks.size());
    JsonObject first = checks.getJsonObject(0);
    assertEquals("bar", first.getString("id"));
    assertEquals("UP", first.getString("status"));
    JsonObject second = checks.getJsonObject(1);
    assertEquals("new-check", second.getString("id"));
    assertEquals("UP", second.getString("status"));
  }
}
