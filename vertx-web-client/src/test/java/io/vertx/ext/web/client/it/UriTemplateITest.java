package io.vertx.ext.web.client.it;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.core.Vertx.vertx;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class UriTemplateITest {

  private HttpClient client = vertx().createHttpClient();

  private static Vertx vertx;

  private static final String GREETING = "Hello from UriTemplateVerticle!";

  @BeforeClass
  public static void deploy(TestContext context) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(UriTemplateVerticle.class.getName(), context.asyncAssertSuccess());
  }


  @Test
  public void greetingFromUriTemplateTest(TestContext testContext) {
    UriTemplate greetingTemplate = UriTemplate.of("http://{host}:{port}/{greeting}");
    String greetUri = greetingTemplate.expandToString(Variables
                                                        .variables()
                                                        .set("host", "localhost")
                                                        .set("port", "8080")
                                                        .set("greeting", "hola")

    );

    client.request(HttpMethod.GET, 8089, "localhost", greetUri)
      .compose(HttpClientRequest::send)
      .onComplete(testContext.asyncAssertSuccess(httpClientResponse -> {
        testContext.assertEquals(200, httpClientResponse.statusCode());
        testContext.assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version());
        httpClientResponse.body().onComplete(testContext.asyncAssertSuccess(bufferAsyncResult -> {
          testContext.assertEquals(GREETING, bufferAsyncResult.toString());
        }));
      }));
  }


  @Test
  public void invalidCharacterInRequest(TestContext testContext) {
    UriTemplate greetingTemplate = UriTemplate.of("http://{host}:{port}/{greeting}");
    String greetIncorrectUri = greetingTemplate.expandToString(Variables
                                                                 .variables()
                                                                 .set("host", "localhost")
                                                                 .set("port", "8081")
                                                                 .set("greeting", "hola")

    );
    client.request(HttpMethod.GET, 8089, "localhost", greetIncorrectUri)
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(testContext.asyncAssertSuccess(httpClientResponse -> {
        testContext.assertEquals(404, httpClientResponse.statusCode());
        httpClientResponse.body().onComplete(testContext.asyncAssertSuccess(bufferAsyncResult -> {
          testContext.assertEquals("404: Not Found", bufferAsyncResult.toString());
        }));
      }));
  }

  @Test
  public void getJsonResponseFromUriTemplateTest(TestContext testContext) {
    Async async = testContext.async();
    Map<String, String> query = new HashMap<>();
    query.put("firstName", "John");
    query.put("lastName", "Cooper");
    UriTemplate jsonTemplate = UriTemplate.of("http://{host}:{port}/person?firstName={query.firstName}&lastname={query.secondName}/{id}");
    String jsonUri = jsonTemplate.expandToString(Variables
                                                   .variables()
                                                   .set("host", "localhost")
                                                   .set("port", "8088")
                                                   .set("id", "12345")
                                                   .set("query", query)
    );


    client.request(HttpMethod.GET, 8088, "localhost", jsonUri)
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(httpClientResponseAsyncResult -> {
        if (httpClientResponseAsyncResult.succeeded()) {
          testContext.assertEquals(200, httpClientResponseAsyncResult.result().statusCode());
          httpClientResponseAsyncResult.result().body(bufferAsyncResult -> {
            if (bufferAsyncResult.succeeded()) {
              JsonObject jsonObject = bufferAsyncResult.result().toJsonObject();
              testContext.assertEquals("John", jsonObject.getString("name"));
              testContext.assertEquals("New York", jsonObject.getString("from"));
              testContext.assertEquals("45", jsonObject.getString("age"));
            }
            async.complete();
          });
        } else {
          testContext.fail(httpClientResponseAsyncResult.cause().getMessage());
        }
      });
  }

  @Test
  public void expansionMultipleVariablesTest(TestContext testContext) {
    UriTemplate variablesTemplate = UriTemplate.of("https//{first}/{second}/{third}/{ids}");
    final String variablesUri = variablesTemplate.expandToString(Variables.variables()
                                                                   .set("first", "subpathA")
                                                                   .set("second", "subpathB")
                                                                   .set("third", "subpathC")
                                                                   .set("ids", Arrays.asList("123", "456")));


    client.request(HttpMethod.GET, 8087, "localhost", variablesUri)
      .compose(httpClientRequest -> httpClientRequest.send())
      .onComplete(testContext.asyncAssertSuccess(httpClientResponse -> {
        assertEquals(200, httpClientResponse.statusCode());
        assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version());
        httpClientResponse.body().onComplete(testContext.asyncAssertSuccess(body -> {
          assertEquals("multivariables in uri template OK!", body.toString());
        }));
      }));
  }

  @AfterClass
  public static void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

}
