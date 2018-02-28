package io.vertx.ext.web.handler.impl.logger;

import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

public class ClientIPParameter extends BaseParameter {

  public ClientIPParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    SocketAddress inetSocketAddress = context.request().remoteAddress();
    if (inetSocketAddress == null) {
      return null;
    }
    return inetSocketAddress.host();
  }
}
