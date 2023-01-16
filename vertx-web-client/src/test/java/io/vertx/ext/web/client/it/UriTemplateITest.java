package io.vertx.ext.web.client.it;


import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.core.Vertx.vertx;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class UriTemplateITest {

  private HttpClient client = vertx().createHttpClient();

  @BeforeAll
  public static void deploy(VertxTestContext vertxTestContext) {
    Checkpoint checkpoint = vertxTestContext.checkpoint();
    vertxTestContext.verify(() -> {
      vertx().deployVerticle(UriTemplateVerticle.class.getName());
      checkpoint.flag();
    });
  }

  @Test
  @DisplayName("greetingFromUriTemplateTest")
  void greetingFromUriTemplateTest(VertxTestContext context) {
    client.request(HttpMethod.GET, 8080, "localhost", "/greeting")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(context.succeeding(httpClientResponse -> {
        assertAll(
          () -> assertEquals(200, httpClientResponse.statusCode()),
          () -> assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version())
        );
        httpClientResponse.body().onComplete(context.succeeding(body -> {
          assertEquals("Hello from UriTemplateVerticle!", body.toString());
          context.completeNow();
        }));
      }))
      .onFailure(context::failNow);
  }

  @Test
  @DisplayName("invalidPortTest")
  void invalidCharacterInRequest(VertxTestContext vertxTestContext) {
    client.request(HttpMethod.GET, 8081, "localhost", "/greeting")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(vertxTestContext.succeeding(httpClientResponse -> {
        assertAll(
          () -> assertEquals(404, httpClientResponse.statusCode()));
        vertxTestContext.completeNow();
      }));
  }

  @Test
  @DisplayName("getJsonResponseFromUriTemplateTest")
  void getJsonResponseFromUriTemplateTest(VertxTestContext context) {
    Checkpoint asynResponseCheck = context.checkpoint();
    context.verify(() -> {
      client.request(HttpMethod.GET, 8081, "localhost", "/person/12345")
        .compose(httpClientRequest -> httpClientRequest.send())
        .onComplete(context.succeeding(httpClientResponse -> {
          assertEquals(200, httpClientResponse.statusCode());
          httpClientResponse.body().onComplete(bufferAsyncResult -> {
            JsonObject jsonObject = bufferAsyncResult.result().toJsonObject();
            assertEquals(jsonObject.getString("name"), "John");
            assertEquals(jsonObject.getString("age"), "45");
          });
          asynResponseCheck.flag();
        }));
    });
  }

  @Test
  @DisplayName("expansionMultipleVariablesTest")
  void expansionMultipleVariablesTest(VertxTestContext context) {
    client.request(HttpMethod.GET, 8082, "localhost", "/subpathA/subpathB/subpathC/123,456")
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(context.succeeding(httpClientResponse -> {
        assertAll(
          () -> assertEquals(200, httpClientResponse.statusCode()),
          () -> assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version())
        );
        httpClientResponse.body().onComplete(context.succeeding(body -> {
          assertEquals("multivariables in uri template OK!", body.toString());
          context.completeNow();
        }));
      }))
      .onFailure(context::failNow);

  }

}
