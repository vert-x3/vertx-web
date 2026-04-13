package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.junit.Assert;
import org.junit.Test;

public class SockJSCORSTest extends WebTestBase {

  public SockJSCORSTest() {
    super(ReportMode.FORBIDDEN);
  }

  @Test
  public void testSockJSInternalCORSHandling() {
    router
      .route()
      .handler(BodyHandler.create());
    SockJSProtocolTest.installTestApplications(router, vertx);
    HttpResponse<Buffer> resp = webClient.get("/echo/info?t=21321")
      .putHeader(HttpHeaders.ORIGIN.toString(), "http://example.com")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    Assert.assertEquals("http://example.com", resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
    Assert.assertEquals("true", resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
  }

  @Test
  public void testNoConflictsSockJSAndCORSHandler() {
    router
      .route()
      .handler(CorsHandler.create().addOriginWithRegex(".*").allowCredentials(false))
      .handler(BodyHandler.create());
    SockJSProtocolTest.installTestApplications(router, vertx);
    //If the SockJS handles the CORS stuff, it would reply with allow credentials true and allow origin example.com
    HttpResponse<Buffer> resp = webClient.get("/echo/info?t=21321")
      .putHeader(HttpHeaders.ORIGIN.toString(), "http://example.com")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    Assert.assertEquals("*", resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toString()));
    Assert.assertFalse(resp.headers().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString()));
  }

  @Test(expected = IllegalStateException.class)
  public void testNoConflictsSockJSAndCORSHandlerBadSetup() {
    router
      .route()
      .handler(BodyHandler.create())
      .handler(CorsHandler.create().addOriginWithRegex(".*").allowCredentials(false));
    SockJSProtocolTest.installTestApplications(router, vertx);
  }

  @Test
  public void testNoConflictsSockJSAndCORSHandlerBadSetupLenient() {
    try {
      System.setProperty("io.vertx.web.router.setup.lenient", "true");
      router
        .route()
        .handler(BodyHandler.create())
        .handler(CorsHandler.create().addOriginWithRegex(".*").allowCredentials(false));
      SockJSProtocolTest.installTestApplications(router, vertx);
    } finally {
      System.clearProperty("io.vertx.web.router.setup.lenient");
    }
  }
}
