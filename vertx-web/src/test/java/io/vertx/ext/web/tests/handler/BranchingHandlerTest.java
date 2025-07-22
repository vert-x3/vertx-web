package io.vertx.ext.web.tests.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.BranchingHandler;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.Test;

public class BranchingHandlerTest extends WebTestBase {

  @Test
  public void testHeaderBasedBranching() throws Exception {
    router.get("/branching").handler(BranchingHandler.create(
      rc -> rc.request().getHeader("Foo") != null,
      rc -> rc.response().end("true"),
      rc -> rc.response().end("false")
    ));
    testRequest(HttpMethod.GET, "/branching", 200, "OK", "false");
    testRequest(HttpMethod.GET, "/branching", request -> {
      request.putHeader("Foo", "123");
    }, 200, "OK", "true");
  }

  @Test
  public void testHeaderBasedBranchingToNextHandlers() throws Exception {
    router.get("/branching").handler(BranchingHandler.create(
      rc -> rc.request().getHeader("Foo") != null,
      rc -> rc.response().end("handler #1")
    ));
    router.get("/branching").handler(rc -> rc.response().end("handler #2"));
    testRequest(HttpMethod.GET, "/branching", 200, "OK", "handler #2");
    testRequest(HttpMethod.GET, "/branching", request -> {
      request.putHeader("Foo", "123");
    }, 200, "OK", "handler #1");
  }
}
