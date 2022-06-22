package io.vertx.ext.web.handler;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.ext.web.handler.HttpException.httpStatusCodeOf;
import static org.junit.Assert.*;

public class HttpExceptionTest {

  @Test
  public void testHttpException() {
    HttpException exception = new HttpException(401);
    assertEquals(401, exception.getStatusCode());
    assertEquals("[401]: Unauthorized", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void testHttpExceptionTryCatch() {
    HttpException exception = new HttpException(401);
    switch (httpStatusCodeOf(exception)) {
      case 401:
        assertEquals("[401]: Unauthorized", exception.getMessage());
        break;
      default:
        throw new RuntimeException("Oops!");
    }
  }

  @Test
  public void testHttpExceptionCallee() {
    HttpException exception = new HttpException(401).setCallee(XFrameHandler.create(XFrameHandler.DENY));

    final AtomicInteger cnt = new AtomicInteger();

    switch (httpStatusCodeOf(exception)) {
      case 401:
        exception
          .catchFrom(XFrameHandler.class, err -> {
            cnt.incrementAndGet();
          })
          .catchFrom(XFrameHandler.class, err -> {
            cnt.incrementAndGet();
          });
        break;
    }

    assertEquals(2, cnt.get());
  }
}
