package io.vertx.ext.healthchecks;

import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HealthCheckTestBase {

  Vertx vertx;
  HealthCheckHandler handler;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    handler = HealthCheckHandler.create(vertx, getAuthProvider());
    router.get("/health*").handler(handler);

    // Only for authentication tests
    router.post("/post-health/*").handler(BodyHandler.create());
    router.post("/post-health*").handler(handler);

    AtomicBoolean done = new AtomicBoolean();
    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(8080, ar -> done.set(ar.succeeded()));
    await().untilAtomic(done, is(true));

    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 8080;
  }

  AuthProvider getAuthProvider() {
    return null;
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    vertx.close(v -> done.set(v.succeeded()));
    await().untilAtomic(done, is(true));
  }

  static JsonObject get(int status) {
    String json = RestAssured.get("/health")
      .then()
      .statusCode(status)
      .header("content-type", "application/json;charset=UTF-8")
      .extract().asString();
    return new JsonObject(json);
  }

  static JsonObject get(String path, int status) {
    String json = RestAssured.get("/health/" + path)
      .then()
      .statusCode(status)
      .header("content-type", "application/json;charset=UTF-8")
      .extract().asString();
    return new JsonObject(json);
  }
}
