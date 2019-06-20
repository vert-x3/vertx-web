package io.vertx.ext.web;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonDecoder;

/**
 * Converter and Codec for {@link io.vertx.ext.web.Http2PushMapping}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.Http2PushMapping} original class using Vert.x codegen.
 */
public class Http2PushMappingConverter implements JsonDecoder<Http2PushMapping, JsonObject> {

  public static final Http2PushMappingConverter INSTANCE = new Http2PushMappingConverter();

  @Override public Http2PushMapping decode(JsonObject value) { return (value != null) ? new Http2PushMapping(value) : null; }

  @Override public Class<Http2PushMapping> getTargetClass() { return Http2PushMapping.class; }
}
