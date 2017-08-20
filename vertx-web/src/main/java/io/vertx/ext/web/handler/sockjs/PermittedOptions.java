package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Specify a match to allow for inbound and outbound traffic using the
 * {@link BridgeOptions}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Deprecated
@DataObject
public class PermittedOptions extends io.vertx.ext.bridge.PermittedOptions {

  public PermittedOptions() {
    super();
  }

  public PermittedOptions(PermittedOptions that) {
    super(that);
  }

  public PermittedOptions(JsonObject json) {
    super(json);
  }
}
