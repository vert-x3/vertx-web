package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.web.validation.testutils.TestRequest.*;

@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterBuilderSecurityHandlerOptionalTest extends BaseRouterBuilderTest {

  private static final String SECURITY_TESTS = "src/test/resources/specs/security_optional_test.yaml";

  private static final RouterBuilderOptions FACTORY_OPTIONS = new RouterBuilderOptions()
    .setRequireSecurityHandlers(true)
    .setMountNotImplementedHandler(false);

  @Test
  public void mountSingle(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder
        .securityHandler("api_key", SimpleAuthenticationHandler.create()
          .authenticate(ctx -> Future.failedFuture(new HttpException(401, "Oops!"))))
        .operation("pets")
        .handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .end()
        );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets")
        .expect(statusCode(200), statusMessage("OK"))
        .send(testContext, checkpoint)
    );
  }
}
