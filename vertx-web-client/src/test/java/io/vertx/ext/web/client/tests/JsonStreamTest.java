package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.ReportHandlerFailures;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the behavior of the {@link io.vertx.ext.web.codec.impl.JsonStreamBodyCodec}.
 */
@ExtendWith(VertxExtension.class)
@ReportHandlerFailures
public class JsonStreamTest {

  private WebClient client;

  @BeforeEach
  public void setup(Vertx vertx, VertxTestContext testContext) {
    client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));

    vertx.createHttpServer().requestHandler(req -> {
      int count = Integer.valueOf(req.getParam("count"));
      String separator = req.getParam("separator");
      if (separator.equalsIgnoreCase("nl")) {
        separator = "\n";
      }
      if (separator.equalsIgnoreCase("bl")) {
        separator = "\r\n";
      }
      req.response().setChunked(true);
      for (int i = 0; i < count; i++) {
        // Send chunks...
        JsonObject json = new JsonObject().put("count", i).put("data", "some message");
        Buffer buffer = json.toBuffer();
        req.response().write(buffer.getBuffer(0, i));
        req.response().write(buffer.getBuffer(i, buffer.length()));
        req.response().write(separator);
      }
      req.response().end();
    }).listen(8080).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  public void testSimpleStream(VertxTestContext testContext) {
    AtomicInteger counter = new AtomicInteger();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(testContext::failNow)
      .handler(event -> {
        JsonObject object = event.objectValue();
        assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> testContext.completeNow());

    client.get("/?separator=nl&count=10").as(BodyCodec.jsonStream(parser)).send().onComplete(x -> {
      if (x.failed()) {
        testContext.failNow(x.cause());
      }
    });
  }

  @Test
  public void testSimpleStreamUsingBlankLine(VertxTestContext testContext) {
    AtomicInteger counter = new AtomicInteger();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(testContext::failNow)
      .handler(event -> {
        JsonObject object = event.objectValue();
        assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> testContext.completeNow());

    client.get("/?separator=bl&count=10").as(BodyCodec.jsonStream(parser)).send().onComplete(x -> {
      if (x.failed()) {
        testContext.failNow(x.cause());
      }
    });
  }

}
