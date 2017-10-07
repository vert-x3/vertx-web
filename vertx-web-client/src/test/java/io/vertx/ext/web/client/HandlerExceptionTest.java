package io.vertx.ext.web.client;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class HandlerExceptionTest {

  private Vertx vertx;


  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end("OK"))
      .listen(8080, tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test(timeout = 5000)
  public void testThatCallbackErrorAreReported(TestContext tc) {
    Async async = tc.async();
    vertx.exceptionHandler(t -> {
      tc.assertEquals(t.getMessage(), "Expected exception");
      async.complete();
    });

    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "")
      .send(resp -> {
        tc.assertTrue(resp.succeeded());
        tc.assertTrue(Context.isOnEventLoopThread());
        throw new RuntimeException("Expected exception");
      });
  }

}
