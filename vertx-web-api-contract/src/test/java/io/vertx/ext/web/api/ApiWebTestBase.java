package io.vertx.ext.web.api;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApiWebTestBase extends WebTestBase {

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
    webClient = WebClient.wrap(client, new WebClientOptions().setConnectTimeout(Integer.MAX_VALUE).setIdleTimeout(Integer.MAX_VALUE).setIdleTimeoutUnit(TimeUnit.MILLISECONDS));
  }

  @Override
  public void tearDown() throws Exception {
    if (webClient != null) {
      try {
        webClient.close();
      } catch (IllegalStateException e) {
      }
    }
    super.tearDown();
  }

  public void testRequestWithBufferResponse(HttpMethod method, String path, String contentType, Buffer obj, int statusCode, String statusMessage, Buffer expected, String expectedContentType) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    HttpClientRequest req = client
      .request(method, 8080, "localhost", path)
      .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
      .handler(res -> {
        if (expected != null) {
          assertEquals(statusCode, res.statusCode());
          assertEquals(statusMessage, res.statusMessage());
          assertEquals(expectedContentType, res.getHeader(HttpHeaders.CONTENT_TYPE));
          res.bodyHandler(buff -> {
            buff = normalizeLineEndingsFor(buff);
            assertEquals(expected, buff);
            latch.countDown();
          });
        } else {
          assertEquals(statusCode, res.statusCode());
          assertEquals(statusMessage, res.statusMessage());
          latch.countDown();
        }
      });
    if (obj == null) req.end();
    else req.end(obj);
    awaitLatch(latch);
  }

  public void testRequestWithJSON(HttpMethod method, String path, Buffer obj, int statusCode, String statusMessage) throws Exception {
    testRequestWithJSON(method, path, obj, statusCode, statusMessage, null);
  }

  public void testRequestWithJSON(HttpMethod method, String path, Buffer obj, int statusCode, String statusMessage, Buffer expected) throws Exception {
    testRequestWithBufferResponse(method, path, "application/json", obj, statusCode, statusMessage, expected, "application/json");
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

  public void testRequestWithResponseContentTypeCheck(HttpMethod method, String path, int statusCode, String contentType, List<String> acceptableContentTypes) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client
      .request(method, 8080, "localhost", path)
      .putHeader("Accept", String.join(", ", acceptableContentTypes))
      .handler(res -> {
        assertEquals(statusCode, res.statusCode());
        assertEquals(contentType, res.getHeader(HttpHeaders.CONTENT_TYPE));
        latch.countDown();
      }).end();
    awaitLatch(latch);
  }

}
