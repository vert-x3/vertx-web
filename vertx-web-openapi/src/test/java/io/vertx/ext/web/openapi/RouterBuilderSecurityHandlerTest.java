package io.vertx.ext.web.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
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
        try {
          routerBuilder
            .securityHandler("oauth").bindBlocking(config -> OAuth2AuthHandler.create(vertx, null))
            .securityHandler("oauth").bindBlocking(config -> APIKeyHandler.create(null))
            .securityHandler("oauth").bindBlocking(config -> APIKeyHandler.create(null))
            .securityHandler("oauth").bindBlocking(config -> APIKeyHandler.create(null))
            .securityHandler("oauth").bindBlocking(config -> APIKeyHandler.create(null));

          testContext.completeNow();
        } catch (RuntimeException e) {
          testContext.failNow(e);
        }
      });
  }

  @Test
  public void someProvided(Vertx vertx, VertxTestContext testContext) {
    // this is allowed, as it defines a existing security scheme
    RouterBuilder.create(vertx, SECURITY_TESTS)
      .onFailure(testContext::failNow)
      .onSuccess(routerBuilder -> {
        try {
          routerBuilder.securityHandler("oauth")
            .bindBlocking(config -> OAuth2AuthHandler.create(vertx, null));

          testContext.completeNow();
        } catch (RuntimeException e) {
          testContext.failNow(e);
        }
      });
  }

  @Test
  public void typpoNotProvided(Vertx vertx, VertxTestContext testContext) {
    // this is allowed, as it defines a existing security scheme
    RouterBuilder.create(vertx, SECURITY_TESTS)
      .onFailure(testContext::failNow)
      .onSuccess(routerBuilder -> {
        try {
          routerBuilder.securityHandler("oauth3")
            .bindBlocking(config -> OAuth2AuthHandler.create(vertx, null));

          testContext.failNow("Should not reach here");
        } catch (RuntimeException e) {
          testContext.completeNow();
        }
      });
  }

  @Test
  public void asyncBindHandler(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, SECURITY_TESTS)
      .compose(routerBuilder ->
        routerBuilder
          .securityHandler("oauth")
          .bind(config -> OAuth2AuthHandler.create(vertx, null))
      )
      .onSuccess(routerBuilder -> {
        testContext.verify(() -> assertThat(routerBuilder).isNotNull());
        testContext.completeNow();
      })
      .onFailure(testContext::failNow);
  }
}
