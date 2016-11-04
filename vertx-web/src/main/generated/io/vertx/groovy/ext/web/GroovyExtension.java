package io.vertx.groovy.ext.web;
public class GroovyExtension {
  public static <U>io.vertx.core.Future<java.lang.Object> map(io.vertx.ext.web.handler.sockjs.BridgeEvent j_receiver, java.util.function.Function<java.lang.Boolean, java.lang.Object> mapper) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.map(mapper != null ? new java.util.function.Function<java.lang.Boolean, java.lang.Object>() {
      public java.lang.Object apply(java.lang.Boolean t) {
        java.lang.Boolean o = t;
        java.lang.Object p = mapper.apply(o);
        return io.vertx.lang.groovy.ConversionHelper.unwrap(p);
      }
    } : null));
  }
  public static java.util.Map<String, Object> rawMessage(io.vertx.ext.web.handler.sockjs.BridgeEvent j_receiver) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonObject(j_receiver.rawMessage());
  }
  public static java.util.Map<String, Object> getRawMessage(io.vertx.ext.web.handler.sockjs.BridgeEvent j_receiver) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonObject(j_receiver.getRawMessage());
  }
  public static io.vertx.ext.web.handler.sockjs.BridgeEvent setRawMessage(io.vertx.ext.web.handler.sockjs.BridgeEvent j_receiver, java.util.Map<String, Object> message) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.setRawMessage(message != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(message) : null));
    return j_receiver;
  }
  public static io.vertx.ext.web.RoutingContext put(io.vertx.ext.web.RoutingContext j_receiver, java.lang.String key, java.lang.Object obj) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.put(key,
      io.vertx.lang.groovy.ConversionHelper.unwrap(obj)));
    return j_receiver;
  }
  public static <T>java.lang.Object get(io.vertx.ext.web.RoutingContext j_receiver, java.lang.String key) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.get(key));
  }
  public static <T>java.lang.Object remove(io.vertx.ext.web.RoutingContext j_receiver, java.lang.String key) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.remove(key));
  }
  public static java.util.Map<String, Object> getBodyAsJson(io.vertx.ext.web.RoutingContext j_receiver) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonObject(j_receiver.getBodyAsJson());
  }
  public static java.util.List<Object> getBodyAsJsonArray(io.vertx.ext.web.RoutingContext j_receiver) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonArray(j_receiver.getBodyAsJsonArray());
  }
  public static io.vertx.ext.web.Session put(io.vertx.ext.web.Session j_receiver, java.lang.String key, java.lang.Object obj) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.put(key,
      io.vertx.lang.groovy.ConversionHelper.unwrap(obj)));
    return j_receiver;
  }
  public static <T>java.lang.Object get(io.vertx.ext.web.Session j_receiver, java.lang.String key) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.get(key));
  }
  public static <T>java.lang.Object remove(io.vertx.ext.web.Session j_receiver, java.lang.String key) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.remove(key));
  }
  public static io.vertx.ext.web.handler.sockjs.SockJSHandler bridge(io.vertx.ext.web.handler.sockjs.SockJSHandler j_receiver, java.util.Map<String, Object> bridgeOptions) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.bridge(bridgeOptions != null ? new io.vertx.ext.web.handler.sockjs.BridgeOptions(io.vertx.lang.groovy.ConversionHelper.toJsonObject(bridgeOptions)) : null));
    return j_receiver;
  }
  public static io.vertx.ext.web.handler.sockjs.SockJSHandler bridge(io.vertx.ext.web.handler.sockjs.SockJSHandler j_receiver, java.util.Map<String, Object> bridgeOptions, io.vertx.core.Handler<io.vertx.ext.web.handler.sockjs.BridgeEvent> bridgeEventHandler) {
    io.vertx.lang.groovy.ConversionHelper.wrap(j_receiver.bridge(bridgeOptions != null ? new io.vertx.ext.web.handler.sockjs.BridgeOptions(io.vertx.lang.groovy.ConversionHelper.toJsonObject(bridgeOptions)) : null,
      bridgeEventHandler != null ? event -> bridgeEventHandler.handle(io.vertx.lang.groovy.ConversionHelper.wrap(event)) : null));
    return j_receiver;
  }
}
