package io.vertx.ext.web.handler;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

public class ChainAuthMixHandlerTest extends WebTestBase {

  private static final User USER = User.create(new JsonObject().put("id", "paulo"));

  private final AuthenticationHandler success = SimpleAuthenticationHandler.create()
    .authenticate(ctx -> Future.succeededFuture(USER));


  private final AuthenticationHandler failure = SimpleAuthenticationHandler.create()
    .authenticate(ctx -> Future.failedFuture(new HttpException(401)));

  @Test
  public void testFailOrFailOrSuccess() throws Exception {

    // (failure OR (failure OR success))
    ChainAuthHandler chain =
      ChainAuthHandler.any()
        .add(failure)
        .add(
          ChainAuthHandler.any()
            .add(failure)
            .add(success)
        );


    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());

    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testFailOrSuccessAndFail() throws Exception {

    // (failure OR (sucess AND failure))
    ChainAuthHandler chain =
      ChainAuthHandler.any()
        .add(failure)
        .add(
          ChainAuthHandler.all()
            .add(success)
            .add(failure)
        );


    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());

    testRequest(HttpMethod.GET, "/", 401, "Unauthorized");
  }
}
