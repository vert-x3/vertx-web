package io.vertx.ext.web.client.it;

import java.util.Arrays;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;

public class UriTemplateVerticle extends AbstractVerticle {
  private static final String GREETING = "Hello from UriTemplateVerticle!";

  private static final String MULTIVARMESSAGE = "multivariables in uri template OK!";

  private HttpServer server;

  private JsonObject jsonObject = new JsonObject();

  private String baseUri;

  private String jsonUri;

  @Override
  public void start(Promise<Void> startPromise) {

    jsonObject.put("id", "12345");
    jsonObject.put("name", "John");
    jsonObject.put("age", 45);
    jsonObject.put("from", "New York");

    UriTemplate basicTemplate = UriTemplate.of("/greeting");
    baseUri = basicTemplate.expandToString(Variables
                                             .variables()
                                             .set("host", "localhost")
                                             .set("port", "8080")
    );

    UriTemplate jsonTemplate = UriTemplate.of("/person/{id}");
    jsonUri = jsonTemplate.expandToString(Variables
                                            .variables()
                                            .set("host", "localhost")
                                            .set("port", "8081")
                                            .set("id", "12345")
    );

    UriTemplate variablesTemplate = UriTemplate.of("/{first}/{second}/{third}/{ids}");
    final String variablesUri = variablesTemplate.expandToString(Variables.variables()
                                                                   .set("host", "localhost")
                                                                   .set("port", "8082")
                                                                   .set("first", "subpathA")
                                                                   .set("second", "subpathB")
                                                                   .set("third", "subpathC")
                                                                   .set("ids", Arrays.asList("123", "456"))
    );


    Future<Void> port8080Future = Future.future(voidPromise -> handleDependsPortAndUr(8080, baseUri, this::handleBaseUri));
    Future<Void> port8081Future = Future.future(voidPromise -> handleDependsPortAndUr(8081, jsonUri, this::handleJsonUri));
    Future<Void> port8082Future = Future.future(voidPromise -> handleDependsPortAndUr(8082, variablesUri, this::handlerMultipleVariables));
    CompositeFuture.all(port8080Future, port8081Future, port8082Future)
      .onComplete(compositeFutureAsyncResult -> {
        if (compositeFutureAsyncResult.succeeded()) {
          startPromise.complete();
        } else {
          System.out.println(compositeFutureAsyncResult.cause());
          startPromise.fail(compositeFutureAsyncResult.cause());
        }
      }).onFailure(throwable -> System.out.println(throwable.getMessage()));

  }

  private void handleDependsPortAndUr(Integer port, String expectedUri, Handler<HttpServerRequest> requestHandler) {
    server = vertx.createHttpServer();
    server.requestHandler(httpServerRequest -> {
      if (httpServerRequest.uri().equalsIgnoreCase(expectedUri)) {
        requestHandler.handle(httpServerRequest);
      } else {
        handleError(httpServerRequest.response());
      }
    }).listen(port);
  }

  private void handleBaseUri(HttpServerRequest request) {
    request.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
      .end(GREETING);
  }

  private void handleJsonUri(HttpServerRequest request) {
    request.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      .end(jsonObject.encodePrettily());
  }

  private void handlerMultipleVariables(HttpServerRequest httpServerRequest) {
    httpServerRequest.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
      .end(MULTIVARMESSAGE);
  }

  private void handleError(HttpServerResponse response) {
    response.setStatusCode(404).end("404: Not Found");
  }

}
