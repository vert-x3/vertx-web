package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

public class TransactionServiceImpl implements TransactionService {
  @Override
  public void getTransactionsList(String from, String to, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {

  }

  @Override
  public void putTransaction(JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {

  }
}
