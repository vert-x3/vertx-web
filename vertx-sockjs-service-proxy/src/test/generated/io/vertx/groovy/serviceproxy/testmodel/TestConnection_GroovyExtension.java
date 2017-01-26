package io.vertx.groovy.serviceproxy.testmodel;
public class TestConnection_GroovyExtension {
  public static io.vertx.serviceproxy.testmodel.TestConnection insert(io.vertx.serviceproxy.testmodel.TestConnection j_receiver, java.lang.String name, java.util.Map<String, Object> data, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>> resultHandler) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.insert(name,
      data != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(data) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>>() {
      public void handle(io.vertx.core.AsyncResult<java.lang.String> ar) {
        resultHandler.handle(ar.map(event -> event));
      }
    } : null));
    return j_receiver;
  }
}
