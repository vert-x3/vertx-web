package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonDecoder;

/**
 * Converter and Codec for {@link io.vertx.ext.web.handler.sockjs.BridgeOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.BridgeOptions} original class using Vert.x codegen.
 */
public class BridgeOptionsConverter implements JsonDecoder<BridgeOptions, JsonObject> {

  public static final BridgeOptionsConverter INSTANCE = new BridgeOptionsConverter();

  @Override public BridgeOptions decode(JsonObject value) { return (value != null) ? new BridgeOptions(value) : null; }

  @Override public Class<BridgeOptions> getTargetClass() { return BridgeOptions.class; }
}
