package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterBuilderSecurityHandlerTest extends BaseRouterBuilderTest {

  private static final String SECURITY_TESTS = "src/test/resources/specs/security_test.yaml";

  @Test
  public void allProvided(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, SECURITY_TESTS)
      .onFailure(testContext::failNow)
      .onSuccess(routerBuilder -> {
        routerBuilder.createSecurityHandlers(
          SecurityHandlerProvider.create()
            .add("oauth", config -> Future.succeededFuture(OAuth2AuthHandler.create(vertx, null)))
            .add("third_api_key", config -> Future.succeededFuture(APIKeyHandler.create(null)))
            .add("sibling_second_api_key", config -> Future.succeededFuture(APIKeyHandler.create(null)))
            .add("second_api_key", config -> Future.succeededFuture(APIKeyHandler.create(null)))
            .add("api_key", config -> Future.succeededFuture(APIKeyHandler.create(null))))
          .onFailure(testContext::failNow)
          .onSuccess(v -> testContext.completeNow());
      });
  }

  @Test
  public void someProvided(Vertx vertx, VertxTestContext testContext) {
    // this is allowed, but logs will be printed
    RouterBuilder.create(vertx, SECURITY_TESTS)
      .onFailure(testContext::failNow)
      .onSuccess(routerBuilder -> {
        routerBuilder.createSecurityHandlers(
          SecurityHandlerProvider.create()
            .add("oauth", config -> Future.succeededFuture(OAuth2AuthHandler.create(vertx, null))))
          .onFailure(testContext::failNow)
          .onSuccess(v -> testContext.completeNow());
      });
  }
}
