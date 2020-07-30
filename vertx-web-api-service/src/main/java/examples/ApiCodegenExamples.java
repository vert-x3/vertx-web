package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.docgen.Source;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.service.RouteToEBServiceHandler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.serviceproxy.ServiceBinder;

import static io.vertx.ext.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.ext.json.schema.common.dsl.Schemas.stringSchema;
import static io.vertx.ext.web.validation.builder.Bodies.json;
import static io.vertx.ext.web.validation.builder.Parameters.optionalParam;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@Source
public class ApiCodegenExamples {

  public void mountHandler(EventBus eventBus, Router router, ValidationHandler validationHandler) {
    router
      .get("/hello")
      .handler(validationHandler)
      .handler(
        RouteToEBServiceHandler
          .build(eventBus, "greeters.myapplication", "hello")
      );
  }

  public void mountHandlerWithTimeout(EventBus eventBus, Router router, ValidationHandler validationHandler) {
    router
      .get("/hello")
      .handler(validationHandler)
      .handler(
        RouteToEBServiceHandler
          .build(eventBus, "greeters.myapplication", "hello", new DeliveryOptions().setSendTimeout(1000))
      );
  }

  public void serviceMountExample(EventBus eventBus, Router router, SchemaParser schemaParser) {
    router.get("/api/transactions")
      .handler(
        ValidationHandlerBuilder.create(schemaParser)
          .queryParameter(optionalParam("from", stringSchema()))
          .queryParameter(optionalParam("to", stringSchema()))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(eventBus, "transactions.myapplication", "getTransactionsList")
      );
    router.post("/api/transactions")
      .handler(
        ValidationHandlerBuilder.create(schemaParser)
          .body(json(objectSchema()))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(eventBus, "transactions.myapplication", "putTransaction")
      );
  }

  public void serviceMount(Vertx vertx) {
    // Instatiate the service
    TransactionService transactionService = new TransactionServiceImpl();

    // Mount the service on the event bus
    ServiceBinder transactionServiceBinder = new ServiceBinder(vertx);
    transactionServiceBinder
      .setAddress("transactions.myapplication")
      .register(TransactionService.class, transactionService);
  }

  public void implGetTransactionsListSuccess(String from, String to, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    // Your business logic
    resultHandler.handle(
      Future.succeededFuture(
        ServiceResponse.completedWithJson(new JsonArray())
      )
    );
  }

  public void implGetTransactionsListFailure(String from, String to, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    // Return a failed result
    resultHandler.handle(
      Future.failedFuture(
        new HttpStatusException(555, "Something bad happened")
      )
    );
  }

}
