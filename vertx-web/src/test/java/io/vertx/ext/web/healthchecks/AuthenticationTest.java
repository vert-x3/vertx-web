package io.vertx.ext.web.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.Test;

import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

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

  @Test
  public void testAuthenticationFailed(TestContext tc) {
    httpClient.request(GET, "/health").compose(HttpClientRequest::send).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationSuccessUsingHeader(TestContext tc) {
    httpClient.request(GET, "/health").compose(request -> {
      return request
        .putHeader("X-Username", "admin")
        .putHeader("X-Password", "admin")
        .send();
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(204, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingHeader(TestContext tc) {
    httpClient.request(GET, "/health").compose(request -> {
      return request
        .putHeader("X-Username", "admin")
        .putHeader("X-Password", "wrong password")
        .send();
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationSuccessfulUsingParam(TestContext tc) {
    String requestURI = String.format("/health?X-Username=%s&X-Password=%s", "admin", "admin");
    httpClient.request(GET, requestURI).compose(HttpClientRequest::send).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(204, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingParam(TestContext tc) {
    String requestURI = String.format("/health?X-Password=%s", "admin");
    httpClient.request(GET, requestURI).compose(HttpClientRequest::send).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationSuccessfulUsingForm(TestContext tc) {
    httpClient.request(POST, "/post-health").compose(request -> {
      return request
        .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
        .send(String.format("X-Username=%s&X-Password=%s", "admin", "admin"));
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(204, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingForm(TestContext tc) {
    httpClient.request(POST, "/post-health").compose(request -> {
      return request
        .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
        .send(String.format("X-Username=%s&X-Password=%s", "admin", "not-my-password"));
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationSuccessfulUsingBody(TestContext tc) {
    httpClient.request(POST, "/post-health").compose(request -> {
      return request
        .putHeader(CONTENT_TYPE, "application/json")
        .send("{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}");
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(204, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingBody(TestContext tc) {
    httpClient.request(POST, "/post-health").compose(request -> {
      return request
        .putHeader(CONTENT_TYPE, "application/json")
        .send("{\"X-Username\":\"admin\", \"X-Password\":\"not my password\"}");
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfMissingContentType(TestContext tc) {
    httpClient.request(POST, "/post-health").compose(request -> {
      return request.send("{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}");
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfMissingBody(TestContext tc) {
    testAuthenticationFailedUsingBody(tc, HttpClientRequest::send);
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfInvalidBody(TestContext tc) {
    testAuthenticationFailedUsingBody(tc, req -> req.send("not-json"));
  }

  private void testAuthenticationFailedUsingBody(TestContext tc, Function<HttpClientRequest, Future<HttpClientResponse>> sender) {
    httpClient.request(POST, "/post-health").compose(request -> {
      request.putHeader(CONTENT_TYPE, "application/json");
      return sender.apply(request);
    }).onComplete(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(403, response.statusCode());
    }));
  }
}
