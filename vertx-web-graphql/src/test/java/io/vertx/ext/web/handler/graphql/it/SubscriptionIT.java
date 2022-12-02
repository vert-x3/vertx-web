package io.vertx.ext.web.handler.graphql.it;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.ApolloWSMessageType;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class SubscriptionIT {
  private final AtomicBoolean worked = new AtomicBoolean(false);

  private static Vertx vertx = Vertx.vertx();
  private HttpClient httpClient;

  @BeforeAll
  public static void deploy(VertxTestContext context) {
    vertx.deployVerticle(SubscriptionExampleServer.class.getName(), context.succeedingThenComplete());
  }

  @AfterEach
  public void check() {
    assert worked.get() : "Messages are not procesed!";
  }

  @Test
  public void exampleTest(VertxTestContext context) {
    final Checkpoint messageCounter = context.checkpoint(8);

    context.verify(()->{
      httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080));
      messageCounter.flag();

      final ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
      set.add("https://vertx.io");
      set.add("https://www.eclipse.org");
      set.add("http://reactivex.io");
      set.add("https://www.graphql-java.com");

      httpClient.webSocket("/graphql")
        .flatMap(webSocket -> {
          JsonObject request = new JsonObject() //initializing the connection
            .put("id", "1")
            .put("type", ApolloWSMessageType.CONNECTION_INIT.getText());
          return webSocket.write(request.toBuffer()).map(webSocket);
        })
        .onSuccess(webSocket -> {
          webSocket.handler(message -> context.verify(() -> {
            Assertions.assertNotEquals(0, message.length());
            final JsonObject json = message.toJsonObject();

            switch (json.getString("type")) {
              case "connection_ack":
                JsonObject request = new JsonObject()
                  .put("id", "2")
                  .put("type", ApolloWSMessageType.START.getText())
                  .put("payload", new JsonObject()
                    .put("query", "subscription { links { url, postedBy { name } } }"));
                webSocket.write(request.toBuffer());
                break;
              case "data":
                final JsonObject links = message.toJsonObject()
                  .getJsonObject("payload")
                  .getJsonObject("data")
                  .getJsonObject("links");
                final String url = links.getString("url");
                Assertions.assertTrue(set.remove(url), "Unexpected element " + url);
                break;
              case "complete":
                Assertions.assertTrue(set.isEmpty(), "Links were not received from the server: " + Arrays.toString(set.toArray()));
                worked.set(true);
                webSocket.close();
                httpClient.close();
                context.completeNow();
                break;
              case "ka": //keep alive
                break;
              case "error":
                context.failNow(new IllegalStateException(json.getString("payload")));
                break;
              default:
                context.failNow(new IllegalArgumentException("Unknown type in message " + json));
            }
            messageCounter.flag();
          }));
        })
        .onFailure(context::failNow);
    });
  }
}
