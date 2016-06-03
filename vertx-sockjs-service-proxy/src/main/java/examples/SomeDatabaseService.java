package examples;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@ProxyGen
public interface SomeDatabaseService {

  void save(String collection, JsonObject document, Handler<AsyncResult<Void>> result);

  @Fluent
  SomeDatabaseService foo(String collection, JsonObject document, Handler<AsyncResult<Void>> result);

  static SomeDatabaseService createProxy(Vertx vertx, String address) {
    return null;
  }
}
