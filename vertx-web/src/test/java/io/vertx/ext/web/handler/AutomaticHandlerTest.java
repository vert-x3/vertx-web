package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

public class AutomaticHandlerTest extends WebTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testOptionsAllMethodsAllowed() throws Exception {
    router.route("/allmethods").handler(ctx -> ctx.response().end());
    testRequest(HttpMethod.OPTIONS, "/allmethods", null, resp -> {
      assertEquals("OPTIONS,GET,HEAD,POST,PUT,DELETE,TRACE,CONNECT,PATCH,OTHER", resp.getHeader("Allow"));
    }, 204, "No Content", null);
  }

  @Test
  public void testOptionsGetPostAllowed() throws Exception {
    router.route("/getpost").method(HttpMethod.GET).method(HttpMethod.POST).handler(ctx -> ctx.response().end());
    testRequest(HttpMethod.OPTIONS, "/getpost", null, resp -> {
      assertEquals("POST,GET", resp.getHeader("Allow"));
    }, 204, "No Content", null);
  }

  @Test
  public void testOptionsAlreadyDeclared() throws Exception {
    router.route("/optionspath").method(HttpMethod.OPTIONS).handler(ctx -> {
      ctx.response().putHeader("Allow", "CUSTOM");
      ctx.response().setStatusCode(204).end();
    });
    testRequest(HttpMethod.OPTIONS, "/optionspath", null, resp -> {
      assertEquals("CUSTOM", resp.getHeader("Allow"));
    }, 204, "No Content", null);
  }

  @Test
  public void testIncorrectVerb() throws Exception {
    router.post("/posthandler").handler(ctx -> ctx.response().end("Hello World!"));
    testRequest(HttpMethod.PUT, "/posthandler", null, resp -> {
      assertEquals("POST", resp.getHeader("Allow"));
    }, 405, "Method Not Allowed", null);
  }

  @Test
  public void testIncorrectVerbRequired() throws Exception {
    router.post("/posthandler2").handler(ctx -> ctx.response().end("Hello World!"));
    testRequest(HttpMethod.GET, "/posthandler2", null, null,
      404, "Not Found", null);
    testRequest(HttpMethod.HEAD, "/posthandler2", null, null,
      404, "Not Found", null);
  }

  @Test
  public void testFailedContentNegotiation() throws Exception {
    router.post("/xmlhandler").consumes("application/xml").handler((ctx) -> ctx.response().end("XML"));
    testRequest(HttpMethod.POST, "/xmlhandler", req -> {
      req.putHeader("Content-Type", "application/json");
    }, null, 415, "Unsupported Media Type", null);
  }

  @Test
  public void testSuccessfulContentNegotiation() throws Exception {
    router.post("/xmlhandler2").consumes("application/xml").handler((ctx) -> ctx.response().end("XML"));
    testRequest(HttpMethod.POST, "/xmlhandler2", req -> {
      req.putHeader("Content-Type", "application/xml");
    }, null, 200, "OK", "XML");
  }

  @Test
  public void testFailedAcceptFulfill() throws Exception {
    router.get("/xmlproducer").produces("application/xml").handler((ctx) -> ctx.response().end("XML"));
    testRequest(HttpMethod.GET, "/xmlproducer", req -> {
      req.putHeader("Accept", "application/json");
    }, null, 406, "Not Acceptable", null);
  }

  @Test
  public void testSuccessfulAcceptFulfill() throws Exception {
    router.get("/xmlproducer2").produces("application/xml").handler((ctx) -> ctx.response().end("XML"));
    testRequest(HttpMethod.GET, "/xmlproducer2", req -> {
      req.putHeader("Accept", "application/xml");
    }, null, 200, "OK", null);
  }
}
