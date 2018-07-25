package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class ApiCodegenExamples {

  public void example1(OpenAPI3RouterFactory routerFactory) {
    routerFactory.addHandlerByOperationId("operationId", routingContext -> {
      RequestParameters parameters = routingContext.get("parsedParameters");
      // Process the request
    });
  }

  public void example2(Handler<AsyncResult<OperationResult>> resultHandler) {
    resultHandler.handle(
      Future.succeededFuture(
        OperationResult.completedWithPlainText(Buffer.buffer("Hello world!"))
      )
    );
  }

  public void example3(Vertx vertx){
    TransactionService service = new TransactionServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    MessageConsumer<JsonObject> serviceConsumer = serviceBinder.register(TransactionService.class, service);
  }

}
