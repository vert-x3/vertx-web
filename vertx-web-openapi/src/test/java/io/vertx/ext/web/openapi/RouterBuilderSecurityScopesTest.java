package io.vertx.ext.web.openapi;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.impl.ScopedAuthentication;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.web.validation.testutils.TestRequest.statusCode;
import static io.vertx.ext.web.validation.testutils.TestRequest.statusMessage;
import static io.vertx.ext.web.validation.testutils.TestRequest.testRequest;

@ExtendWith(VertxExtension.class)
@Timeout(1000)
public class RouterBuilderSecurityScopesTest extends BaseRouterBuilderTest {

    private static final String SECURITY_TESTS = "src/test/resources/specs/security_scopes_test.yaml";
    private static final String REQUIRED_SCOPES_KEY = "required_scopes";

    private static final RouterBuilderOptions FACTORY_OPTIONS = new RouterBuilderOptions()
            .setRequireSecurityHandlers(true)
            .setMountNotImplementedHandler(false);

    @Test
    void noScopes(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(
                vertx,
                SECURITY_TESTS,
                testContext,
                this::registerHandlers)
                .onComplete(
                        h ->
                                testRequest(client, HttpMethod.GET, "/no_scopes")
                                        // Expect there to be no required scopes for this route
                                        .expect(statusCode(200), statusMessage(new JsonArray().encode()))
                                        .send(testContext, checkpoint));
    }

    @Test
    void oneScope(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(
                vertx,
                SECURITY_TESTS,
                testContext,
                this::registerHandlers)
                .onComplete(
                        h ->
                                testRequest(client, HttpMethod.GET, "/one_scope_required")
                                        .expect(statusCode(200), statusMessage("[\"read\"]"))
                                        .send(testContext, checkpoint));
    }

    @Test
    void twoScopes(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        loadBuilderAndStartServer(
                vertx,
                SECURITY_TESTS,
                testContext,
                this::registerHandlers)
                .onComplete(
                        h ->
                                testRequest(client, HttpMethod.GET, "/two_scopes_required")
                                        .expect(statusCode(200), statusMessage("[\"read\",\"write\"]"))
                                        .send(testContext, checkpoint));
    }

    private void registerHandlers(RouterBuilder routerBuilder) {
        routerBuilder.setOptions(FACTORY_OPTIONS);

        routerBuilder
                .operation("oneScopeRequired")
                .handler(
                        routingContext ->
                                routingContext
                                        .response()
                                        .setStatusCode(200)
                                        .setStatusMessage(Json.CODEC.toString(routingContext.get(REQUIRED_SCOPES_KEY)))
                                        .end());

        routerBuilder
                .operation("noScopesRequired")
                .handler(
                        routingContext ->
                                routingContext
                                        .response()
                                        .setStatusCode(200)
                                        .setStatusMessage(Json.CODEC.toString(routingContext.get(REQUIRED_SCOPES_KEY)))
                                        .end());

        routerBuilder
                .operation("twoScopesRequired")
                .handler(
                        routingContext ->
                                routingContext
                                        .response()
                                        .setStatusCode(200)
                                        .setStatusMessage(Json.CODEC.toString(routingContext.get(REQUIRED_SCOPES_KEY)))
                                        .end());

        routerBuilder.securityHandler("bearerAuth", new TestAuthHandler());
    }

    /**
     * A {@link ScopedAuthentication} auth handler which stores it's required scopes on the context for later validation.
     */
    private class TestAuthHandler implements ScopedAuthentication<TestAuthHandler>, AuthenticationHandler {

        private final List<String> requiredScopes;

        public TestAuthHandler() {
            this(new ArrayList<>());
        }

        public TestAuthHandler(Collection<String> scopes) {
            this.requiredScopes = new ArrayList<>(scopes);
        }

        @Override
        public TestAuthHandler withScope(String scope) {
            return this.withScopes(Stream.of(scope).collect(Collectors.toList()));
        }

        @Override
        public TestAuthHandler withScopes(List<String> scopes) {
            return new TestAuthHandler(
                    Stream.concat(
                                    scopes.stream(),
                                    this.requiredScopes.stream())
                            .collect(Collectors.toList()));
        }

        @Override
        public void handle(RoutingContext event) {
            event.put(REQUIRED_SCOPES_KEY, this.requiredScopes);
            event.next();
        }
    }

}
