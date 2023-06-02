package io.vertx.ext.web.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.runner.RunWith;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.GET;

@RunWith(VertxUnitRunner.class)
public abstract class HealthCheckTestBase {

  private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

  private HttpServer httpServer;

  HttpClient httpClient;

  Vertx vertx;
  HealthCheckHandler healthCheckHandler;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    healthCheckHandler = HealthCheckHandler.create(vertx, getAuthProvider());

    Router router = Router.router(vertx);
    setupRouter(router, healthCheckHandler);

    Async async = tc.async();

    httpServer = vertx.createHttpServer();
    httpServer
      .requestHandler(router)
      .listen(0)
      .onComplete(tc.asyncAssertSuccess(v -> async.complete()));

    async.awaitSuccess();

    httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(httpServer.actualPort()));
  }

  protected abstract void setupRouter(Router router, HealthCheckHandler healthCheckHandler);

  protected AuthenticationProvider getAuthProvider() {
    return null;
  }

  @After
  public void tearDown(TestContext tc) {
    Future<Void> httpServerClose = httpServer != null ? httpServer.close() : Future.succeededFuture();
    Future<Void> httpClientClose = httpClient != null ? httpClient.close() : Future.succeededFuture();
    Future.all(httpServerClose, httpClientClose)
      .andThen(v -> vertx.close()).onComplete(tc.asyncAssertSuccess());
  }

  Future<JsonObject> getCheckResult(String requestURI, int status) {
    return httpClient.request(GET, requestURI).compose(request -> {
      return request.send().compose(resp -> {
        if (resp.statusCode() != status) {
          return Future.failedFuture(new ComparisonFailure("Unexpected status code", String.valueOf(status), String.valueOf(resp.statusCode())));
        }
        String contentType = resp.headers().get(CONTENT_TYPE);
        if (!APPLICATION_JSON_CHARSET_UTF_8.equals(contentType)) {
          return Future.failedFuture(new ComparisonFailure("Unexpected content type", APPLICATION_JSON_CHARSET_UTF_8, contentType));
        }
        return resp.body().map(buffer -> {
          return buffer != null && buffer.length() > 0 ? buffer.toJsonObject() : null;
        });
      });
    });
  }
}
