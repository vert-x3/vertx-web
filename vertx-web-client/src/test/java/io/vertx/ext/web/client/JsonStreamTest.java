package io.vertx.ext.web.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Checks the behavior of the {@link io.vertx.ext.web.codec.impl.JsonStreamBodyCodec}.
 */
@RunWith(VertxUnitRunner.class)
public class JsonStreamTest {

  private Vertx vertx;
  private WebClient client;

  @Before
  public void setup(TestContext tc) {
    vertx = Vertx.vertx();
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
    }).listen(8080, tc.asyncAssertSuccess());
  }

  @After
  public void close(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }


  @Test
  public void testSimpleStream(TestContext tc) {
    AtomicInteger counter = new AtomicInteger();
    Async async = tc.async();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(tc::fail)
      .handler(event -> {
        JsonObject object = event.objectValue();
        tc.assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        tc.assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> async.complete());

    client.get("/?separator=nl&count=10").as(BodyCodec.jsonStream(parser)).send(x -> {
      if (x.failed()) {
        tc.fail(x.cause());
      }
    });
  }

  @Test
  public void testSimpleStreamUsingBlankLine(TestContext tc) {
    AtomicInteger counter = new AtomicInteger();
    Async async = tc.async();
    JsonParser parser = JsonParser.newParser().objectValueMode()
      .exceptionHandler(tc::fail)
      .handler(event -> {
        JsonObject object = event.objectValue();
        tc.assertEquals(counter.getAndIncrement(), object.getInteger("count"));
        tc.assertEquals("some message", object.getString("data"));
      })
      .endHandler(x -> async.complete());

    client.get("/?separator=bl&count=10").as(BodyCodec.jsonStream(parser)).send(x -> {
      if (x.failed()) {
        tc.fail(x.cause());
      }
    });
  }

}
