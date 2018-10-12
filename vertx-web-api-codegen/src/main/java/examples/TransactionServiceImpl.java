package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class TransactionServiceImpl implements TransactionService {
  @Override
  public void getTransactionsList(String from, String to, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

  }

  @Override
  public void putTransaction(JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

  }
}
