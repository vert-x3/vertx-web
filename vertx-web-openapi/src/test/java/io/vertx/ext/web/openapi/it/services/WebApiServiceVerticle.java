package io.vertx.ext.web.openapi.it.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.it.models.Transaction;
import io.vertx.ext.web.openapi.it.persistence.TransactionPersistence;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.serviceproxy.ServiceBinder;

public class WebApiServiceVerticle extends AbstractVerticle {

  private HttpServer httpServer;

  private ServiceBinder serviceBinder;

  private MessageConsumer<JsonObject> consumer;

  private TransactionPersistence persistence;

  @Override
  public void start(Promise<Void> startPromise) {
    startTransactionService();
    startHttpServer().onComplete(startPromise);
  }

  private void startTransactionService() {
    serviceBinder = new ServiceBinder(vertx);
    this.persistence = TransactionPersistence.create();
    TransactionManagerService transactionManagerService = TransactionManagerService.create(persistence);

    consumer = serviceBinder
      .setAddress("transactions_manager.myapp")
      .register(TransactionManagerService.class, transactionManagerService);
  }

  private Future<Void> startHttpServer() {
    return RouterBuilder.create(vertx, "openapi.json")
      .onFailure(Throwable::printStackTrace) // In case the contract loading failed print the stacktrace
      .compose(routerBuilder -> {
        // Mount services on event bus based on extensions
        routerBuilder.mountServicesFromExtensions();

        // createTransaction operation customized and set the post response status code to 201

        routerBuilder.operation("createTransaction")
          .handler(routingContext -> {
            RequestParameters requestParameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
            JsonObject jsonObject = requestParameters.body().getJsonObject();
            Transaction transaction = new Transaction(
              jsonObject.getString("id"),
              jsonObject.getString("message"),
              jsonObject.getString("from"),
              jsonObject.getString("to"),
              jsonObject.getDouble("value")
            );

            routingContext.response()
              .setStatusCode(201)
              .end(transaction.toJson().encode());
            persistence.addTransaction(transaction);

          });

        // Generate the router
        Router router = routerBuilder.createRouter();
        router.errorHandler(400, ctx -> {
          System.out.println("Bad Request " + ctx.failure());
        });
        httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost")).requestHandler(router);
        return httpServer.listen(8080).mapEmpty();
      });
  }
}
