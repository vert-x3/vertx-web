package io.vertx.ext.web.client;

import io.vertx.core.http.HttpMethod;
import io.vertx.uritemplate.UriTemplate;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class UriTemplateTest extends WebClientTestBase {

  private static final String EURO_SYMBOL = "\u20AC";

  @Test
  public void testUriTemplate() throws Exception {
    UriTemplate template = UriTemplate.of("/test{?name}{&currency}");
    testRequest(client ->
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
  public void testQueryParam() throws Exception {
    UriTemplate template = UriTemplate.of("/{?name}{&currency}");
    testRequest(client ->
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
  public void testAbsoluteURI() throws Exception {
    UriTemplate template = UriTemplate.of("http://{host}:{port}/{?name}{&currency}");
    testRequest(client ->
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
  public void testTemplateExpansion() throws Exception {
    Map<String, String> query = new HashMap<>();
    query.put("color", "red");
    query.put("currency", EURO_SYMBOL);
    testRequest(client -> client.request(HttpMethod.GET, UriTemplate.of("/{action}?username={username}{&query*}"))
      .setTemplateParam("action", "info")
      .setTemplateParam("username", "vietj")
      .setTemplateParam("query", query), req -> {
      assertEquals("/info", req.path());
      assertEquals("vietj", req.getParam("username"));
      assertEquals("red", req.getParam("color"));
      assertEquals(EURO_SYMBOL, req.getParam("currency"));
    });
  }
}
