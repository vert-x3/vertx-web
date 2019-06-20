package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.ext.web.handler.sockjs.PermittedOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.PermittedOptions} original class using Vert.x codegen.
 */
public class PermittedOptionsConverter implements JsonCodec<PermittedOptions, JsonObject> {

  public static final PermittedOptionsConverter INSTANCE = new PermittedOptionsConverter();

  @Override public JsonObject encode(PermittedOptions value) { return (value != null) ? value.toJson() : null; }

  @Override public PermittedOptions decode(JsonObject value) { return (value != null) ? new PermittedOptions(value) : null; }

  @Override public Class<PermittedOptions> getTargetClass() { return PermittedOptions.class; }
}
