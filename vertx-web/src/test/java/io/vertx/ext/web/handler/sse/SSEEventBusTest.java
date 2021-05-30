package io.vertx.ext.web.handler.sse;

import io.vertx.core.eventbus.DeliveryOptions;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class SSEEventBusTest extends SSEBaseTest {

  @Test
  public void forwardSimpleStringThroughEventBus() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    String sentMsg = "just-a-string";
    eventSource()
      .messageHandler(msg -> {
        assertEquals(sentMsg, msg);
        latch.countDown();
      })
      .connectHandler(SSE_EVENTBUS_ENDPOINT, res -> {
        assertFalse(res.failed());
        vertx.eventBus().publish(EB_ADDRESS, sentMsg);
      });
    awaitLatch(latch);
  }

  @Test
  public void forwardEventThroughEventBus() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    String sentMsg = "string-as-event";
    String eventName = "just-an-event";
    eventSource()
      .messageHandler(msg -> {
        throw new RuntimeException("onMessage shouldn't have been called, onEvent should have");
      })
      .eventHandler(eventName, msg -> {
        assertEquals(sentMsg, msg);
        latch.countDown();
      })
      .connectHandler(SSE_EVENTBUS_ENDPOINT, res -> {
        assertFalse(res.failed());
        vertx.eventBus().publish(EB_ADDRESS, sentMsg, new DeliveryOptions().addHeader(SSEHeaders.EVENT.toString(), eventName));
      });
    awaitLatch(latch);
  }

  @Test
  public void forwardDataThroughEventBusWithId() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    String sentMsg = "string-with-id";
    String idName = "something-identified";
    EventSource es = eventSource();
    es
      .messageHandler(msg -> {
        assertEquals(sentMsg, msg.replace("\n", "")); // FIXME: this does not conform to the spec: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format
        assertEquals(idName, es.lastId());
        latch.countDown();
      })
      .connectHandler(SSE_EVENTBUS_ENDPOINT, res -> {
        assertFalse(res.failed());
        vertx.eventBus().publish(EB_ADDRESS, sentMsg, new DeliveryOptions().addHeader(SSEHeaders.ID.toString(), idName));
      });
    awaitLatch(latch);
  }

  @Test
  public void forwardEventWithId() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    String sentMsg = "string-with-event-and-id";
    String idName = "some-event-id";
    String eventName = "the-event-with-id";
    EventSource es = eventSource();
    es
      .messageHandler(msg -> {
        throw new RuntimeException("onMessage shouldn't have been called, onEvent should have");
      })
      .eventHandler(eventName, msg -> {
        assertEquals(sentMsg, msg);
        assertEquals(idName, es.lastId());
        latch.countDown();
      })
      .connectHandler(SSE_EVENTBUS_ENDPOINT, res -> {
        assertFalse(res.failed());
        vertx.eventBus().publish(EB_ADDRESS, sentMsg, new DeliveryOptions().addHeader(SSEHeaders.EVENT.toString(), eventName).addHeader(SSEHeaders.ID.toString(), idName));
      });
    awaitLatch(latch);
  }

}
