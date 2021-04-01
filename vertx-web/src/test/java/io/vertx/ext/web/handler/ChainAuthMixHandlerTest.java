package io.vertx.ext.web.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.impl.AuthenticationHandlerImpl;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

public class ChainAuthMixHandlerTest extends WebTestBase {

  private final AuthenticationHandler success = new AuthenticationHandlerImpl((authInfo, resultHandler) -> resultHandler.handle(Future.succeededFuture(User.create(new JsonObject())))) {
    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler) {
      handler.handle(Future.succeededFuture(JsonObject::new));
    }
  };

  private final AuthenticationHandler failure = new AuthenticationHandlerImpl((authInfo, resultHandler) -> resultHandler.handle(Future.failedFuture("Oops!"))) {
    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler) {
      handler.handle(Future.failedFuture(new HttpException(401)));
    }
  };

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
