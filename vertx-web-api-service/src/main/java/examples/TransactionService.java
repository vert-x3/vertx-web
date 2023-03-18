package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
interface TransactionService {
  Future<ServiceResponse> getTransactionsList(String from, String to, ServiceRequest context);
  Future<ServiceResponse> putTransaction(JsonObject body, ServiceRequest context);
}
