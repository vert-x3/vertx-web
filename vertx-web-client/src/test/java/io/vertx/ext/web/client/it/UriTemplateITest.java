package io.vertx.ext.web.client.it;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.core.Vertx.vertx;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class UriTemplateITest {

  private HttpClient client = vertx().createHttpClient();

  private Vertx vertx;

  @Before
  public void deploy(TestContext context) {
    vertx = Vertx.vertx();
    Async async = context.async();
    vertx.deployVerticle(UriTemplateVerticle.class.getName());
    async.complete();
  }

  @Test
  public void greetingFromUriTemplateTest(TestContext testContext) {
    client.request(HttpMethod.GET, 8089, "localhost", "/greeting")
      .compose(HttpClientRequest::send)
      .onComplete(testContext.asyncAssertSuccess(resp -> {
        testContext.assertEquals(200, resp.statusCode());
        testContext.assertEquals(HttpVersion.HTTP_1_1, resp.version());
        resp.body(bufferAsyncResult -> {
          if (bufferAsyncResult.succeeded()) {
            testContext.assertEquals("Hello from UriTemplateVerticle!", bufferAsyncResult.result().toString());
          } else {
            System.out.println(bufferAsyncResult.cause().getMessage());
          }
        });

      })).onFailure(throwable -> throwable.getMessage());

  }

  @Test
  public void invalidCharacterInRequest(TestContext testContext) {
    client.request(HttpMethod.GET, 8087, "localhost", "/greeting")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(testContext.asyncAssertSuccess(resp -> {
        testContext.assertEquals(404, resp.statusCode());
      }));
  }

  @Test
  public void getJsonResponseFromUriTemplateTest(TestContext testContext) {
    client.request(HttpMethod.GET, 8088, "localhost", "/person/12345")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(testContext.asyncAssertSuccess(httpClientResponse -> {
        assertEquals(200, httpClientResponse.statusCode());
        httpClientResponse.body().onComplete(bufferAsyncResult -> {
          JsonObject jsonObject = bufferAsyncResult.result().toJsonObject();
          assertEquals(jsonObject.getString("name"), "John");
          assertEquals(jsonObject.getString("age"), "45");
        });
      }));
  }

  @Test
  public void expansionMultipleVariablesTest(TestContext testContext) {
    client.request(HttpMethod.GET, 8087, "localhost", "/subpathA/subpathB/subpathC/123,456")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(testContext.asyncAssertSuccess(httpClientResponse -> {
        assertEquals(200, httpClientResponse.statusCode());
        assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version());
        httpClientResponse.body().onComplete(testContext.asyncAssertSuccess(body -> {
          assertEquals("multivariables in uri template OK!", body.toString());
        }));
      }));
  }

}
