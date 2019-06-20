package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonDecoder;

/**
 * Converter and Codec for {@link io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions} original class using Vert.x codegen.
 */
public class SockJSHandlerOptionsConverter implements JsonDecoder<SockJSHandlerOptions, JsonObject> {

  public static final SockJSHandlerOptionsConverter INSTANCE = new SockJSHandlerOptionsConverter();

  @Override public SockJSHandlerOptions decode(JsonObject value) { return (value != null) ? new SockJSHandlerOptions(value) : null; }

  @Override public Class<SockJSHandlerOptions> getTargetClass() { return SockJSHandlerOptions.class; }
}
