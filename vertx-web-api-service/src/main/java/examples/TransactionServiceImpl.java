package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class TransactionServiceImpl implements TransactionService {
  @Override
  public void getTransactionsList(String from, String to, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {

  }

  @Override
  public void putTransaction(JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {

  }
}
