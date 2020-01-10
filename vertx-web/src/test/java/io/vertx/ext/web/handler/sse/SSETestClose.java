package io.vertx.ext.web.handler.sse;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class SSETestClose extends SSETestBase {

	private void waitSafely() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ie) {}
	}

	@Test
	public void closeHandlerOnServer() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
		final EventSource eventSource = eventSource();
		eventSource.connect("/sse?token=" + TOKEN, handler -> {
				assertTrue(handler.succeeded());
				assertNotNull(connection);
                sseHandler.closeHandler(sse -> {
                    latch.countDown();
                });
                waitSafely();
				eventSource.close(); /* closed by client */
			});
		awaitLatch(latch);
	}

	@Test
  public void closeHandlerOnClient() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    final EventSource eventSource = eventSource();
		eventSource.onClose(handler -> latch.countDown());
		eventSource.connect("/sse?token=" + TOKEN, handler -> {
      assertNotNull(connection);
      connection.close();
    });
		awaitLatch(latch);
	}

}
