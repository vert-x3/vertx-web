package io.vertx.serviceproxy.test;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.SockJSProxyTestBase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JSBusTest extends SockJSProxyTestBase {

  @Test
  public void testBusFail() {
    vertx.eventBus().consumer("client_registered", msg -> {
      msg.reply("fail_this", res -> {
        assertTrue(res.failed());
        assertNotNull(res.cause());
        ReplyException exception = (ReplyException) res.cause();
        assertEquals(ReplyFailure.RECIPIENT_FAILURE, exception.failureType());
        assertEquals(520, exception.failureCode());
        assertEquals("client_failure_message", exception.getMessage());
        testComplete();
      });
    });
    vertx.deployVerticle("bus_test_fail.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusReconnect() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      testComplete();
    });
    vertx.deployVerticle("bus_test_reconnect.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend1() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      testComplete();
    });
    vertx.deployVerticle("bus_test_send_1.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend2() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      testComplete();
    });
    vertx.deployVerticle("bus_test_send_2.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend3() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("done", msg -> testComplete());
    vertx.deployVerticle("bus_test_send_3.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend4() {
    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      count.incrementAndGet();
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("the_address_fail", msg -> {
      count.incrementAndGet();
      msg.fail(0, "the_failure");
    });
    vertx.eventBus().consumer("done", msg -> {
      assertEquals(2, count.get());
      testComplete();
    });
    vertx.deployVerticle("bus_test_send_4.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend5() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("done", msg -> testComplete());
    vertx.deployVerticle("bus_test_send_5.js", ar -> assertTrue(ar.succeeded()));
    await();
  }

  @Test
  public void testBusSend6() {
    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      count.incrementAndGet();
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("the_address_fail", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value_fail", msg.headers().get("the_header_name"));
      count.incrementAndGet();
      msg.fail(0, "the_failure");
    });
    vertx.eventBus().consumer("done", msg -> {
      assertEquals(2, count.get());
      testComplete();
    });
    vertx.deployVerticle("bus_test_send_6.js", ar -> assertTrue(ar.succeeded()));
    await();
  }
}
