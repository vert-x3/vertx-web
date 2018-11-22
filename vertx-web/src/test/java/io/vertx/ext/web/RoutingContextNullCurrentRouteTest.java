package io.vertx.ext.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;

@RunWith(VertxUnitRunner.class)
public class RoutingContextNullCurrentRouteTest {

    static final int PORT = 9091;
    private Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(TestVerticle.class.getName(), context.asyncAssertSuccess(event -> async.complete()));
    }

    @Test
    public void test(TestContext testContext) {
        HttpClient client =
                vertx.createHttpClient(new HttpClientOptions()
                        .setConnectTimeout(10000));
        Async async = testContext.async();
        HttpClientRequest httpClientRequest =
                client.get(PORT, "127.0.0.1", "/test", httpClientResponse -> {
                    testContext.assertEquals(HttpURLConnection.HTTP_NO_CONTENT, httpClientResponse.statusCode());
                    async.complete();
                }).exceptionHandler(testContext::fail);
        httpClientRequest.end();
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    public static class TestVerticle extends AbstractVerticle {

        @Override
        public void start(Future<Void> startFuture) throws Exception {

            Router router = Router.router(vertx);
            router.get("/test").handler(routingCount ->
                    vertx.setTimer(5000, timerId -> {
                        HttpServerResponse response = routingCount.response();
                        if (routingCount.currentRoute() == null) {
                            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                                    .end();
                        } else {
                            response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT)
                                    .end();
                        }
                    }));

            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(PORT, asyncResult -> {
                        if (asyncResult.succeeded()) {
                            startFuture.complete();
                        } else {
                            startFuture.fail(asyncResult.cause());
                        }
                    });
        }
    }
}
