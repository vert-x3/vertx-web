package io.vertx.ext.web.tests.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import org.junit.jupiter.api.Test;

import static io.vertx.core.http.HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationTest extends HealthCheckTestBase {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    router.get("/health*").handler(healthCheckHandler);
    router.post("/post-health").handler(BodyHandler.create());
    router.post("/post-health").handler(healthCheckHandler);
  }

  @Override
  protected AuthenticationProvider getAuthProvider() {
    return credentials -> {
      JsonObject jsonObject = credentials.toJson();
      if ("admin".equals(jsonObject.getString("X-Username"))
        && "admin".equals(jsonObject.getString("X-Password"))) {
        return Future.succeededFuture(User.create(new JsonObject().put("login", "admin")));
      } else {
        return Future.failedFuture("Not Authorized");
      }
    };
  }

  private int request(RequestOptions options) {
    return httpClient
      .request(options)
      .compose(request -> request
        .send()
        .compose(response -> response
          .end()
          .map(response.statusCode())))
      .await();
  }

  @Test
  public void testAuthenticationFailed() {
    assertEquals(403, request(new RequestOptions().setURI("/health")));
  }

  @Test
  public void testAuthenticationSuccessUsingHeader() {
    assertEquals(204, request(new RequestOptions().setURI("/health")
      .putHeader("X-Username", "admin")
      .putHeader("X-Password", "admin")));
  }

  @Test
  public void testAuthenticationFailedUsingHeader() {
    assertEquals(403, request(new RequestOptions().setURI("/health")
      .putHeader("X-Username", "admin")
      .putHeader("X-Password", "wrong password")));
  }

  @Test
  public void testAuthenticationSuccessfulUsingParam() {
    assertEquals(204, request(new RequestOptions().setURI("/health?X-Username=admin&X-Password=admin")));
  }

  @Test
  public void testAuthenticationFailedUsingParam() {
    assertEquals(403, request(new RequestOptions().setURI("/health?X-Password=admin")));
  }

  @Test
  public void testAuthenticationSuccessfulUsingForm() {
    assertEquals(204, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED),
      "X-Username=admin&X-Password=admin"));
  }

  @Test
  public void testAuthenticationFailedUsingForm() {
    assertEquals(403, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED),
      "X-Username=admin&X-Password=not-my-password"));
  }

  @Test
  public void testAuthenticationSuccessfulUsingBody() {
    assertEquals(204, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, "application/json"),
      "{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}"));
  }

  @Test
  public void testAuthenticationFailedUsingBody() {
    assertEquals(403, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, "application/json"),
      "{\"X-Username\":\"admin\", \"X-Password\":\"not my password\"}"));
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfMissingContentType() {
    assertEquals(403, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health"),
      "{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}"));
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfMissingBody() {
    assertEquals(403, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, "application/json"),
      null));
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfInvalidBody() {
    assertEquals(403, requestWithBody(
      new RequestOptions().setMethod(POST).setURI("/post-health").putHeader(CONTENT_TYPE, "application/json"),
      "not-json"));
  }

  private int requestWithBody(RequestOptions options, String body) {
    return httpClient
      .request(options)
      .compose(request -> {
        if (body != null) {
          return request.send(body);
        } else {
          return request.send();
        }
      })
      .compose(response -> response
        .end()
        .map(response.statusCode()))
      .await();
  }
}
