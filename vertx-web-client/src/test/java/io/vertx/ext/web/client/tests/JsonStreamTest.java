package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import io.vertx.test.core.VertxRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks the behavior of the {@link io.vertx.ext.web.codec.impl.JsonStreamBodyCodec}.
 */
@VertxTest
public class JsonStreamTest {

  private WebClient client;

  @BeforeEach
  public void setup(Vertx vertx) {
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
    }).listen(8080)
      .await();
  }

  @Test
  public void testSimpleStream(Checkpoint checkpoint) {
    AtomicInteger counter = new AtomicInteger();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(v -> fail())
      .handler(event -> {
        JsonObject object = event.objectValue();
        assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> checkpoint.flag());

    client.get("/?separator=nl&count=10")
      .as(BodyCodec.jsonStream(parser))
      .send()
      .await();;
  }

  @Test
  public void testSimpleStreamUsingBlankLine(Checkpoint checkpoint) {
    AtomicInteger counter = new AtomicInteger();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(Assertions::fail)
      .handler(event -> {
        JsonObject object = event.objectValue();
        assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> checkpoint.flag());

    client.get("/?separator=bl&count=10")
      .as(BodyCodec.jsonStream(parser))
      .send()
      .await();
  }
}
