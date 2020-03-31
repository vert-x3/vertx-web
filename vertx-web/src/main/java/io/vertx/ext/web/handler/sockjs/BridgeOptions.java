package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Deprecated: use {@link SockJSBridgeOptions} instead.
 */
@Deprecated
@DataObject
public class BridgeOptions extends SockJSBridgeOptions {

  public BridgeOptions(JsonObject json) {
    super(json);
  }
}
