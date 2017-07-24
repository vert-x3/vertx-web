package io.vertx.ext.web;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.CountDownLatch;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class WebTestWithWebClientBase extends WebTestBase {

  public enum FormType {
    MULTIPART("multipart/form-data"),
    FORM_URLENCODED("application/x-www-form-urlencoded");

    public String headerValue;

    FormType(String headerValue) {
      this.headerValue = headerValue;
    }
  }

  public WebClient webClient;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    webClient = WebClient.wrap(client);
  }

  @Override
  public void tearDown() throws Exception {
    if (webClient != null) {
      webClient.close();
    }
    super.tearDown();
  }

  public void testRequestWithJSON(HttpMethod method, String path, JsonObject jsonObject, int statusCode, String statusMessage) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    webClient.request(method, 8080, "localhost", path).sendJsonObject(jsonObject, (ar) -> {
      assertEquals(statusCode, ar.result().statusCode());
      assertEquals(statusMessage, ar.result().statusMessage());
      latch.countDown();
    });
    awaitLatch(latch);
  }

  public void testRequestWithForm(HttpMethod method, String path, FormType formType, MultiMap formMap, int statusCode, String statusMessage) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    HttpRequest<Buffer> request = webClient
      .request(method, 8080, "localhost", path);
    request
      .putHeader("Content-Type", formType.headerValue)
      .sendForm(formMap, (ar) -> {
        assertEquals(statusCode, ar.result().statusCode());
        assertEquals(statusMessage, ar.result().statusMessage());
        latch.countDown();
      });
    awaitLatch(latch);
  }
}
