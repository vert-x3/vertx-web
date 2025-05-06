package io.vertx.ext.web.sstore.caffeine.tests;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.caffeine.CaffeineSessionStore;
import io.vertx.ext.web.tests.handler.SessionHandlerTestBase;
import org.junit.Test;

/**
 * @author <a href="mailto:lazarbulic@gmail.com">Lazar Bulic</a>
 */
public class CaffeineSessionHandlerTest extends SessionHandlerTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = CaffeineSessionStore.create(vertx);
  }

  @Test
  public void testRetryTimeout() throws Exception {
    assertTrue(doTestSessionRetryTimeout() < 3000);
  }

  @Test
  public void test2123() throws Exception {
    SessionHandler sessionHandler = SessionHandler.create(CaffeineSessionStore.create(vertx))
      .setSessionTimeout(10_000)
      .setLazySession(true);

    router.clear();

    router.route().handler(sessionHandler);
    router.route().handler(ctx -> {
      ctx.session();
      ctx.response().setStatusCode(500);
      sessionHandler.flush(ctx).onComplete(asyncResult -> {
        // store was skipped, so we signed with a success
        assertTrue(asyncResult.succeeded());
        ctx.end();
      });
    });

    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
  }
}
