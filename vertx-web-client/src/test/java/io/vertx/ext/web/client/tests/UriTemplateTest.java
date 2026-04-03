package io.vertx.ext.web.client.tests;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.uritemplate.ExpandOptions;
import io.vertx.uritemplate.UriTemplate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class UriTemplateTest extends WebClientJUnit5TestBase {

  private static final String EURO_SYMBOL = "\u20AC";

  private void testRequest(VertxTestContext testContext, Function<WebClient, HttpRequest<Buffer>> reqFactory, Consumer<HttpServerRequest> reqChecker) {
    Checkpoint serverChecked = testContext.checkpoint(2);
    server.requestHandler(req -> {
      try {
        reqChecker.accept(req);
        serverChecked.flag();
      } finally {
        req.response().end();
      }
    });
    startServer();
    HttpRequest<Buffer> builder = reqFactory.apply(webClient);
    builder.send().await();
    builder.send().await();
  }

  @Test
  public void testUriTemplate(VertxTestContext testContext) {
    UriTemplate template = UriTemplate.of("/test{?name}{&currency}");
    testRequest(testContext, client ->
        client.get(template)
          .setTemplateParam("name", "Julien")
          .setTemplateParam("currency", "\u20AC"),
      req -> {
        assertEquals("name=Julien&currency=%E2%82%AC", req.query());
        assertEquals("Julien", req.params().get("name"));
        assertEquals(EURO_SYMBOL, req.params().get("currency"));
      });
  }

  @Test
  public void testQueryParam(VertxTestContext testContext) {
    UriTemplate template = UriTemplate.of("/{?name}{&currency}");
    testRequest(testContext, client ->
        client.get(template)
          .setTemplateParam("name", "Julien")
          .setTemplateParam("currency", EURO_SYMBOL)
          .setQueryParam("city", "Marseille"),
      req -> {
        assertEquals("name=Julien&currency=%E2%82%AC&city=Marseille", req.query());
        assertEquals("Julien", req.params().get("name"));
        assertEquals(EURO_SYMBOL, req.params().get("currency"));
        assertEquals("Marseille", req.params().get("city"));
      });
  }

  @Test
  public void testAbsoluteURI(VertxTestContext testContext) {
    UriTemplate template = UriTemplate.of("http://{host}:{port}/{?name}{&currency}");
    testRequest(testContext, client ->
        client.requestAbs(HttpMethod.GET, template)
          .setTemplateParam("host", "localhost")
          .setTemplateParam("port", "8080")
          .setTemplateParam("name", "Julien")
          .setTemplateParam("currency", EURO_SYMBOL)
          .setQueryParam("city", "Marseille"),
      req -> {
        assertEquals("name=Julien&currency=%E2%82%AC&city=Marseille", req.query());
        assertEquals("Julien", req.params().get("name"));
        assertEquals(EURO_SYMBOL, req.params().get("currency"));
        assertEquals("Marseille", req.params().get("city"));
      });
  }

  @Test
  public void testTemplateExpansion(VertxTestContext testContext) {
    Map<String, String> query = new HashMap<>();
    query.put("color", "red");
    query.put("currency", EURO_SYMBOL);
    testRequest(testContext, client -> {
      HttpRequest<Buffer> request = client.request(HttpMethod.GET, UriTemplate.of("/{action}?username={username}{&query*}"))
        .setTemplateParam("action", "info")
        .setTemplateParam("query", query);
      // Missing variable is accepted
      assertEquals("/info?username=&color=red&currency=%E2%82%AC", request.uri());
      request.setTemplateParam("username", "vietj");
      assertEquals("/info?username=vietj&color=red&currency=%E2%82%AC", request.uri());
      return request;
    }, req -> {
      assertEquals("/info", req.path());
      assertEquals("vietj", req.getParam("username"));
      assertEquals("red", req.getParam("color"));
      assertEquals(EURO_SYMBOL, req.getParam("currency"));
    });
  }

  @Test
  public void testIncomplete() {
    UriTemplate template = UriTemplate.of("/{missing}");
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setDefaultPort(8080)
      .setDefaultHost("localhost")
      .setTemplateExpandOptions(new ExpandOptions()
        .setAllowVariableMiss(false)));
    HttpRequest<Buffer> request = webClient.get(template);
    assertThrows(NoSuchElementException.class, () -> request.send().await());
  }
}
