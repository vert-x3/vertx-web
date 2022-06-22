package io.vertx.ext.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.impl.DigestAuthHandlerImpl;
import org.junit.Test;

import static io.vertx.ext.web.handler.HttpException.httpStatusCode;
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
    switch (httpStatusCode(exception)) {
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
    assertTrue(exception.thrownBy(XFrameHandler.class));
  }
}
