package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.vertx.ext.web.validation.testutils.TestRequest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterBuilderSecurityWithFactoryTest extends BaseRouterBuilderTest {

  private static final String SECURITY_TESTS = "src/test/resources/specs/security_test.yaml";
  private static final String GLOBAL_SECURITY_TESTS = "src/test/resources/specs/global_security_test.yaml";

  private static final RouterBuilderOptions FACTORY_OPTIONS = new RouterBuilderOptions()
    .setRequireSecurityHandlers(true)
    .setMountNotImplementedHandler(false);

  @Test
  public void mountSingle(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder
        .setOptions(FACTORY_OPTIONS)
        .createAuthenticationHandlers(
          AuthenticationHandlerProvider.create()
            .add(
              "api_key",
              config -> Future.succeededFuture(mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1")))))

        .onSuccess(v -> routerBuilder.operation("listPetsSingleSecurity").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key"))
          .end()
        ));

    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_single_security")
        .expect(statusCode(200), statusMessage("1"))
        .send(testContext, checkpoint));
  }

  @Test
  public void mountAnd(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsAndSecurity").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key",
          "third_api_key"))
        .end()
      );

      routerBuilder.securityHandler("api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1"))
      );

      routerBuilder.securityHandler("second_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
      );

      routerBuilder.securityHandler("third_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("third_api_key", "3"))
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_and_security")
        .expect(statusCode(200), statusMessage("1-2-3"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountAndFirstOneFailing(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
        routerBuilder.setOptions(FACTORY_OPTIONS);

        routerBuilder.operation("listPetsAndSecurity").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key",
            "third_api_key"))
          .end()
        );

        routerBuilder.securityHandler("api_key",
          mockFailingAuthHandler(routingContext -> routingContext.put("api_key", "1"))
        );

        routerBuilder.securityHandler("second_api_key",
          mockSuccessfulAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
        );

        routerBuilder.securityHandler("third_api_key",
          mockSuccessfulAuthHandler(routingContext -> routingContext.put("third_api_key", "3"))
        );
      }, new AbstractMap.SimpleImmutableEntry<>(401, routingContext ->
        routingContext
          .response()
          .setStatusCode(401)
          .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key", "third_api_key"))
          .end()
      )
    ).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_and_security")
        .expect(statusCode(401), statusMessage("1-null-null"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountOrWithFirstSuccessful(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsOrSecurity").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key",
          "third_api_key"))
        .end()
      );

      routerBuilder.securityHandler("api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1"))
      );

      routerBuilder.securityHandler("second_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
      );

      routerBuilder.securityHandler("third_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("third_api_key", "3"))
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_or_security")
        .expect(statusCode(200), statusMessage("1-null-null"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountOrWithLastSuccessful(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsOrSecurity").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key",
          "third_api_key"))
        .end()
      );

      routerBuilder.securityHandler("api_key",
        mockFailingAuthHandler(routingContext -> routingContext.put("api_key", "1"))
      );

      routerBuilder.securityHandler("second_api_key",
        mockFailingAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
      );

      routerBuilder.securityHandler("third_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("third_api_key", "3"))
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_or_security")
        .expect(statusCode(200), statusMessage("1-2-3"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountOrWithAllFailing(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
        routerBuilder.setOptions(FACTORY_OPTIONS);

        routerBuilder.operation("listPetsOrSecurity").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key",
            "third_api_key"))
          .end()
        );

        routerBuilder.securityHandler("api_key",
          mockFailingAuthHandler(routingContext -> routingContext.put("api_key", "1"))
        );

        routerBuilder.securityHandler("second_api_key",
          mockFailingAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
        );

        routerBuilder.securityHandler("third_api_key",
          mockFailingAuthHandler(routingContext -> routingContext.put("third_api_key", "3"))
        );
      }, new AbstractMap.SimpleImmutableEntry<>(401, routingContext ->
        routingContext
          .response()
          .setStatusCode(401)
          .setStatusMessage(concatenateRoutingContextEntries(routingContext, "api_key", "second_api_key", "third_api_key"))
          .end()
      )
    ).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_or_security")
        .expect(statusCode(401), statusMessage("1-2-3"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountOrAndMixed(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsOrAndSecurity").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(concatenateRoutingContextEntries(
          routingContext,
          "api_key",
          "second_api_key",
          "sibling_second_api_key",
          "third_api_key"
        ))
        .end()
      );

      routerBuilder.securityHandler("api_key",
        mockFailingAuthHandler(routingContext -> routingContext.put("api_key", "1"))
      );

      routerBuilder.securityHandler("second_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("second_api_key", "2"))
      );

      routerBuilder.securityHandler("sibling_second_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("sibling_second_api_key", "3"))
      );

      routerBuilder.securityHandler("third_api_key",
        mockFailingAuthHandler(routingContext -> routingContext.put("third_api_key", "4"))
      );
    }).onComplete(h ->
      testRequest(client, HttpMethod.GET, "/pets_or_and_security")
        .expect(statusCode(200), statusMessage("1-2-3-null"))
        .send(testContext, checkpoint)
    );
  }

  @Test
  public void mountOauth2WithScopes(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, SECURITY_TESTS, testContext.succeeding(routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsOauth2").handler(routingContext -> routingContext
        .response()
        .setStatusCode(200)
        .end()
      );

      // Some oauth2 configuration
      OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
        .setClientId("client-id")
        .setFlow(OAuth2FlowType.AUTH_CODE)
        .setClientSecret("client-secret")
        .setSite("http://localhost:10000"));

      routerBuilder.securityHandler("oauth2", OAuth2AuthHandler.create(vertx, oauth2));

      testContext.verify(() -> {
        Router router = routerBuilder.createRouter();
        Route route = router.getRoutes().get(router.getRoutes().size() - 1);

        assertThat(route)
          .extracting("state")
          .extracting("contextHandlers")
          .asList()
          .filteredOn(new Condition<>(o -> o instanceof OAuth2AuthHandler, "Handler is an OAuth2Handler"))
          .first()
          .extracting("scopes")
          .asList()
          .containsExactlyInAnyOrder("write:pets", "read:pets");
      });
      testContext.completeNow();

    }));
  }

  @Test
  public void mountGlobalSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    final Handler<RoutingContext> handler = routingContext -> {
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage((routingContext.get("message") != null) ? routingContext.get("message") + "-OK" : "OK")
        .end();
    };

    Checkpoint checkpoint = testContext.checkpoint(3);

    loadBuilderAndStartServer(vertx, GLOBAL_SECURITY_TESTS, testContext, routerBuilder -> {
      routerBuilder.setOptions(FACTORY_OPTIONS);

      routerBuilder.operation("listPetsWithoutSecurity").handler(handler);
      routerBuilder.operation("listPetsWithOverride").handler(handler);
      routerBuilder.operation("listPetsWithoutOverride").handler(handler);

      testContext.verify(() ->
        assertThatCode(routerBuilder::createRouter)
          .isInstanceOfSatisfying(RouterBuilderException.class, rfe ->
            assertThat(rfe.type())
              .isEqualTo(ErrorType.MISSING_SECURITY_HANDLER)
          )
      );

      routerBuilder.securityHandler("global_api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("message", "Global"))
      );
      routerBuilder.securityHandler("api_key",
        mockSuccessfulAuthHandler(routingContext -> routingContext.put("message", "Local"))
      );
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/petsWithoutSecurity")
        .expect(statusCode(200), statusMessage("OK"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/petsWithOverride")
        .expect(statusCode(200), statusMessage("Local-OK"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.GET, "/petsWithoutOverride")
        .expect(statusCode(200), statusMessage("Global-OK"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void requireSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, "src/test/resources/specs/router_builder_test.yaml",
      testContext.succeeding(routerBuilder -> {
        routerBuilder.setOptions(FACTORY_OPTIONS);

        routerBuilder.operation("listPets").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("message") + "OK")
          .end()
        );

        testContext.verify(() ->
          assertThatCode(routerBuilder::createRouter)
            .isInstanceOfSatisfying(RouterBuilderException.class, rfe ->
              assertThat(rfe.type())
                .isEqualTo(ErrorType.MISSING_SECURITY_HANDLER)
            )
        );

        routerBuilder.securityHandler("api_key", mockSuccessfulAuthHandler(context -> {
        }));
        routerBuilder.securityHandler("second_api_key", mockSuccessfulAuthHandler(context -> {
        }));
        routerBuilder.securityHandler("third_api_key", mockSuccessfulAuthHandler(context -> {
        }));

        testContext.verify(() ->
          assertThatCode(routerBuilder::createRouter)
            .doesNotThrowAnyException()
        );
        testContext.completeNow();

      }));

  }

  @Test
  public void notRequireSecurityHandler(Vertx vertx, VertxTestContext testContext) {
    RouterBuilder.create(vertx, "src/test/resources/specs/router_builder_test.yaml",
      routerBuilderAsyncResult -> {
        RouterBuilder routerBuilder = routerBuilderAsyncResult.result();

        routerBuilder.setOptions(new RouterBuilderOptions().setRequireSecurityHandlers(false));

        routerBuilder.operation("listPets").handler(routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("message") + "OK")
          .end()
        );

        testContext.verify(() ->
          assertThatCode(routerBuilder::createRouter)
            .doesNotThrowAnyException()
        );

        testContext.completeNow();
      });
  }

  private AuthenticationHandler mockSuccessfulAuthHandler(Handler<RoutingContext> mockHandler) {
    return SimpleAuthenticationHandler.create()
      .authenticate(ctx -> {
        mockHandler.handle(ctx);
        return Future.succeededFuture(User.create(new JsonObject()));
      });
  }

  private AuthenticationHandler mockFailingAuthHandler(Handler<RoutingContext> mockHandler) {
    return SimpleAuthenticationHandler.create()
      .authenticate(ctx -> {
        mockHandler.handle(ctx);
        return Future.failedFuture(new HttpException(401));
      });
  }

  private String concatenateRoutingContextEntries(RoutingContext context, String... entries) {
    return Arrays
      .stream(entries)
      .map(context::get)
      .map(Objects::toString)
      .collect(Collectors.joining("-"));
  }

}
