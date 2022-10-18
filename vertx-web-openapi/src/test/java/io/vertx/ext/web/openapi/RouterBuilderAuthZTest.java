package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.web.validation.testutils.TestRequest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterBuilderAuthZTest extends BaseRouterBuilderTest {

    private static final String SECURITY_TESTS = "src/test/resources/specs/security_test.yaml";

    private static final RouterBuilderOptions FACTORY_OPTIONS = new RouterBuilderOptions()
            .setRequireSecurityHandlers(true)
            .setMountNotImplementedHandler(false);

    @Test
    public void routerBuilderFailsWithAuthZ(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder -> {
            routerBuilder
                    .setOptions(FACTORY_OPTIONS)
                    .securityHandler("api_key")
                    .bindBlocking(config -> mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1")))
                    .operation("listPetsSingleSecurity")
                    .handler(mockAuthorizationHandler(true));

            testContext.verify(() -> {
                assertThatCode(routerBuilder::createRouter)
                        .isInstanceOfSatisfying(IllegalStateException.class, ise ->
                                assertThat(ise.getMessage())
                                        .contains("AUTHORIZATION"));
                checkpoint.flag();
            });
        });
    }

    @Test
    public void mountAuthZSuccess(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder ->
            routerBuilder
                    .setOptions(FACTORY_OPTIONS)
                    .securityHandler("api_key")
                    .bindBlocking(config -> mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1")))
                    .operation("listPetsSingleSecurity")
                    .authorizationHandler(mockAuthorizationHandler(true))
                    .handler(routingContext ->
                        routingContext
                                .response()
                                .setStatusCode(200)
                                .setStatusMessage(routingContext.get("api_key"))
                                .end()))
        .onComplete(h ->
                testRequest(client, HttpMethod.GET, "/pets_single_security")
                        .expect(statusCode(200), statusMessage("1"))
                        .send(testContext, checkpoint));
    }

    @Test
    public void mountAuthZFailure(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(vertx, SECURITY_TESTS, testContext, routerBuilder ->
                routerBuilder
                        .setOptions(FACTORY_OPTIONS)
                        .securityHandler("api_key")
                        .bindBlocking(config -> mockSuccessfulAuthHandler(routingContext -> routingContext.put("api_key", "1")))
                        .operation("listPetsSingleSecurity")
                        .authorizationHandler(mockAuthorizationHandler(false))
                        .handler(routingContext ->
                                routingContext
                                        .response()
                                        .setStatusCode(200)
                                        .setStatusMessage(routingContext.get("api_key"))
                                        .end()))
                .onComplete(h ->
                        testRequest(client, HttpMethod.GET, "/pets_single_security")
                                .expect(statusCode(403), statusMessage("Forbidden"))
                                .send(testContext, checkpoint));
    }

    private AuthorizationHandler mockAuthorizationHandler(boolean authorized) {
        return AuthorizationHandler.create(new Authorization() {
            @Override
            public boolean match(AuthorizationContext authorizationContext) {
                return authorized;
            }

            @Override
            public boolean verify(Authorization authorization) {
                return authorized;
            }
        });
    }

    private AuthenticationHandler mockSuccessfulAuthHandler(Handler<RoutingContext> mockHandler) {
        return SimpleAuthenticationHandler.create()
                .authenticate(ctx -> {
                    mockHandler.handle(ctx);
                    return Future.succeededFuture(User.create(new JsonObject()));
                });
    }
}
