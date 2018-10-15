package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
interface TransactionService {
  void getTransactionsList(String from, String to, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void putTransaction(JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
