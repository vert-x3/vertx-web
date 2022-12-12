package io.vertx.ext.web.handler.graphql.it;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ExampleIT {

  private WebClient webClient;

  Vertx vertx = Vertx.vertx();


  @BeforeAll
  public static void deploy(Vertx vertx, VertxTestContext context) {
    vertx.deployVerticle(ExampleServer.class.getName(), context.succeedingThenComplete());
  }


  interface Checker {
    void check(AsyncResult<HttpResponse<JsonObject>> result);
  }

  @Test
  public void example(VertxTestContext context) {
    JsonObject request = new JsonObject()
      .put("query", "query($secure: Boolean) { allLinks(secureOnly: $secure) { url, postedBy { name } } }")
      .put("variables", new JsonObject().put("secure", true));

    final Checker checker = ar -> {
      JsonObject response = ar.result().body();
      Assertions.assertEquals(ar.result().statusCode(), 200, "Status code is 200");
      long count = response.getJsonObject("data").getJsonArray("allLinks").stream().count();
      Assertions.assertEquals(3L, count);
    };

    verify(context, request, checker, vertx);
  }

  @Test
  public void simperQuery(VertxTestContext context) {
    final JsonObject query = new JsonObject().put("query", "{ allLinks(secureOnly: true) { url, postedBy { name } } }");
    final Checker checker = ar -> {
      JsonObject response = ar.result().body();
      Assertions.assertEquals(ar.result().statusCode(), 200, "Status code is 200");
      long count = response.getJsonObject("data").getJsonArray("allLinks").stream().count();
      Assertions.assertEquals(3L, count);
    };
    verify(context, query, checker, vertx);
  }

  @Test
  public void allLinks(VertxTestContext context) {
    final JsonObject query = new JsonObject()
      .put("query", "{ allLinks(secureOnly: false) { url, postedBy { name } } }");
    final Checker checker = ar -> {
      JsonObject response = ar.result().body();
      Assertions.assertEquals(ar.result().statusCode(), 200, "Status code is 200");
      long count = response.getJsonObject("data").getJsonArray("allLinks").stream().count();
      Assertions.assertEquals(4L, count);
    };
    verify(context, query, checker, vertx);
  }

  @Test
  public void defaultValue(VertxTestContext context) {
    final JsonObject query = new JsonObject()
      .put("query", "{ allLinks{ url, postedBy { name } } }");
    final Checker checker = ar -> {
      JsonObject response = ar.result().body();
      Assertions.assertEquals(ar.result().statusCode(), 200, "Status code is 200");
      long count = response.getJsonObject("data").getJsonArray("allLinks").stream().count();
      Assertions.assertEquals(4L, count);
    };
    verify(context, query, checker, vertx);
  }

  private void verify(VertxTestContext context, JsonObject request, Checker checker, Vertx vertx) {
    Checkpoint checkpoint = context.checkpoint();
    context.verify(() -> {
      webClient = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080));
      checkpoint.flag();
      webClient.post("/graphql")
        .expect(ResponsePredicate.SC_OK)
        .expect(ResponsePredicate.JSON)
        .as(BodyCodec.jsonObject())
        .sendJsonObject(request, ar -> {
          if (!ar.succeeded()) {
            context.failNow(ar.cause());
          }
          try {
            checker.check(ar);
            context.completeNow();
          } catch (Throwable ex) {
            context.failNow(ex);
          }
        });
    });

  }

}
