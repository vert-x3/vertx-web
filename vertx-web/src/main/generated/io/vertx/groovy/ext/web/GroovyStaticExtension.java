package io.vertx.groovy.ext.web;
public class GroovyStaticExtension {
  public static io.vertx.ext.web.handler.sockjs.SockJSHandler create(io.vertx.ext.web.handler.sockjs.SockJSHandler j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> options) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.web.handler.sockjs.SockJSHandler.create(vertx,
      options != null ? new io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions(io.vertx.lang.groovy.ConversionHelper.toJsonObject(options)) : null));
  }
}
