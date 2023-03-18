package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class TransactionServiceImpl implements TransactionService {

  @Override
  public Future<ServiceResponse> getTransactionsList(String from, String to, ServiceRequest context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Future<ServiceResponse> putTransaction(JsonObject body, ServiceRequest context) {
    throw new UnsupportedOperationException();
  }
}
