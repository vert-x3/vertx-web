package io.vertx.ext.web.handler.logger;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

/**
 * Returns the host name of the server to which the request was sent.
 *
 * It is the value of the part before ":" in the Host header value, if any,
 * or the resolved server name, or the server IP address.
 */
class ServerNameParameter extends BaseParameter {

  public ServerNameParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    String hostHeader = context.request().headers().get(HttpHeaderNames.HOST);

    if (hostHeader != null) {
      int portIdx = hostHeader.indexOf(":");
      if (portIdx != -1) {
        return hostHeader.substring(0, portIdx);
      }
    }

    SocketAddress inetSocketAddress = context.request().localAddress();
    if (inetSocketAddress == null) {
      return null;
    }
    return inetSocketAddress.host();
  }
}
