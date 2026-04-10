package io.vertx.ext.web.tests.healthchecks;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

import static io.vertx.core.http.HttpMethod.GET;

@VertxTest
public abstract class HealthCheckTestBase {

  private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

  private HttpServer httpServer;

  HttpClient httpClient;

  Vertx vertx;
  HealthCheckHandler healthCheckHandler;

  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    this.vertx = vertx;
    healthCheckHandler = HealthCheckHandler.create(vertx, getAuthProvider());

    Router router = Router.router(vertx);
    setupRouter(router, healthCheckHandler);

    httpServer = vertx.createHttpServer();
    httpServer
      .requestHandler(router)
      .listen(0)
      .await(20, TimeUnit.SECONDS);

    httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(httpServer.actualPort()));
  }

  protected abstract void setupRouter(Router router, HealthCheckHandler healthCheckHandler);

  protected AuthenticationProvider getAuthProvider() {
    return null;
  }

  @AfterEach
  public void tearDown() throws Exception {
    httpClient
      .close()
      .await(20, TimeUnit.SECONDS);
    httpClient = null;
    httpServer
      .close()
      .await(20, TimeUnit.SECONDS);
    httpServer = null;
  }

  JsonObject getCheckResult(String requestURI, int status) {
    Buffer buffer = httpClient
      .request(GET, requestURI)
      .compose(request -> request
        .send()
        .expecting(HttpResponseExpectation.status(status))
        .expecting(HttpResponseExpectation.JSON)
        .compose(HttpClientResponse::body))
      .await();
    if (buffer != null && buffer.length() > 0) {
      return buffer.toJsonObject();
    }
    return null;
  }
}
