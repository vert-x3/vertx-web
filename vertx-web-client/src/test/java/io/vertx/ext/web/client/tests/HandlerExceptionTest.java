package io.vertx.ext.web.client.tests;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxTest
public class HandlerExceptionTest {

  private Vertx vertx;


  @BeforeEach
  public void setUp(Vertx vertx) {
    this.vertx = vertx;
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end("OK"))
      .listen(8080)
      .await();
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testThatCallbackErrorAreReported(VertxTestContext testContext) {
    vertx.exceptionHandler(t -> {
      assertEquals("Expected exception", t.getMessage());
      testContext.completeNow();
    });

    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "")
      .send().onComplete(resp -> {
        assertTrue(resp.succeeded());
        assertTrue(Context.isOnEventLoopThread());
        throw new RuntimeException("Expected exception");
      });
  }

}
