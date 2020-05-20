package io.vertx.ext.web.handler.sse;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

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
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
    eventSource.onError(error -> {
      // TODO: event type / event name / content
      latch.countDown();
    });
    eventSource.connect(SSE_NO_CONTENT_ENDPOINT, handler -> {});
    awaitLatch(latch);
  }

}
