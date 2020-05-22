package io.vertx.ext.web.handler.sse.impl;

import io.vertx.ext.web.handler.sse.EventSource;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class SSEEventSourceTest extends SSEBaseTest {

  @Test
  public void noContentResponseIsHandled() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.onError(error -> {
      // TODO: event type / event name / content
      latch.countDown();
    });
    eventSource.connect(SSE_NO_CONTENT_ENDPOINT, handler -> {});
    awaitLatch(latch);
  }

  @Test
  public void resetContentResponseIsHandled() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.onError(error -> {
      // TODO: event type / event name / content
      latch.countDown();
    });
    eventSource.connect(SSE_RESET_CONTENT_ENDPOINT, handler -> {});
    awaitLatch(latch);
  }

  @Test
  public void testRetry() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    final EventSource eventSource = eventSource(100);
    AtomicBoolean rejectedOnce = new AtomicBoolean(false);
    AtomicBoolean connected = new AtomicBoolean(false);
    eventSource.onError(error -> {
      rejectedOnce.set(true);
      latch.countDown();
    });
    eventSource.connect(SSE_REJECT_ODDS, handler -> {
      connected.set(true);
      latch.countDown();
    });
    awaitLatch(latch);
    assertTrue("Connection should have been rejected, then retried, then accepted", rejectedOnce.get() && connected.get());
  }

  @Test
  public void testFollowRedirects() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_REDIRECT_ENDPOINT, res -> {
      assertFalse("Redirect should have been followed", res.failed());
      latch.countDown();
    });
    awaitLatch(latch);
  }

  @Test
  public void testReconnect() throws Exception {
    CountDownLatch latch = new CountDownLatch(2); // we should connect twice
    final EventSource eventSource = eventSource();
    eventSource.connect(SSE_RECONNECT_ENDPOINT, res -> {
      assertFalse("Redirect should have been followed", res.failed());
      latch.countDown();
    });
    awaitLatch(latch);
  }

}
