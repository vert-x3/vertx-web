package io.vertx.ext.web.tests;

import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.test.core.VertxTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JUnitReportRouterTest {

  private static final AssertionError ASSERTION_ERROR;

  static {
    AssertionError ae = null;
    try {
      Assert.assertTrue(false);
    } catch (AssertionError e) {
      ae = e;
    }
    ASSERTION_ERROR = ae;
  }

  private Result runTest(Class<?> testClass) {
    try {
      return new JUnitCore().run(new BlockJUnit4ClassRunner(testClass));
    } catch (InitializationError initializationError) {
      throw new AssertionError(initializationError);
    }
  }

  @Test
  public void testRouteHandlerAssertionFailure() {
    RouteHandlerAssertionFailure.SC = null;
    Result result = runTest(RouteHandlerAssertionFailure.class);
    assertEquals(1, result.getFailureCount());
    Throwable exception = result.getFailures().get(0).getException();
    assertEquals(ASSERTION_ERROR, exception);
    assertNotNull(RouteHandlerAssertionFailure.SC);
    assertEquals(500, (int) RouteHandlerAssertionFailure.SC);
  }

  public static class RouteHandlerAssertionFailure extends VertxTestBase {

    public static Integer SC;
    public RouteHandlerAssertionFailure() {
      super(ReportMode.STATELESS);
    }

    @Test
    public void testAssertionError() {
      Router router = Router.router(vertx);
      router.route().handler(rc -> {
        throw ASSERTION_ERROR;
      });
      HttpServer server = vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(0, "localhost")
        .await();
      HttpClient client = vertx.createHttpClient(
        new HttpClientConfig()
          .setDefaultHost("localhost")
          .setDefaultPort(server.actualPort()));
      SC = client.request(HttpMethod.GET, "/").compose(request -> request
          .send()
          .compose(response -> response
            .end()
            .map(response.statusCode()))
      ).await();
      await();
    }
  }

  @Test
  public void testRouteFailureHandlerAssertionFailure() {
    RouteFailureHandlerAssertionFailure.SC = null;
    Result result = runTest(RouteFailureHandlerAssertionFailure.class);
    assertEquals(1, result.getFailureCount());
    Throwable exception = result.getFailures().get(0).getException();
    assertEquals(ASSERTION_ERROR, exception);
    assertNotNull(RouteFailureHandlerAssertionFailure.SC);
    assertEquals(500, (int) RouteFailureHandlerAssertionFailure.SC);
  }

  public static class RouteFailureHandlerAssertionFailure extends VertxTestBase {

    public static Integer SC;
    public RouteFailureHandlerAssertionFailure() {
      super(ReportMode.STATELESS);
    }

    @Test
    public void testAssertionError() {
      Router router = Router.router(vertx);
      router.route().handler(rc -> {
        throw new RuntimeException();
      }).failureHandler(rc -> {
        throw ASSERTION_ERROR;
      });
      HttpServer server = vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(0, "localhost")
        .await();
      HttpClient client = vertx.createHttpClient(
        new HttpClientConfig()
          .setDefaultHost("localhost")
          .setDefaultPort(server.actualPort()));
      SC = client.request(HttpMethod.GET, "/").compose(request -> request
        .send()
        .compose(response -> response
          .end()
          .map(response.statusCode()))
      ).await();
      await();
    }
  }
}
