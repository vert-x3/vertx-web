package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
interface TransactionService {
  void getTransactionsList(String from, String to, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);
  void putTransaction(JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);
}
