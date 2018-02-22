package io.vertx.ext.web.handler.logger;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

/**
 * Returns the port number to which the request was sent.
 *
 * It is the value of the part after ":" in the Host header value, if any,
 * or the server port where the client connection was accepted on.
 */
class LocalPortParameter extends BaseParameter {

  public LocalPortParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    String hostHeader = context.request().headers().get(HttpHeaderNames.HOST);

    if (hostHeader != null) {
      int portIdx = hostHeader.indexOf(":");
      if (portIdx != -1) {
        return hostHeader.substring(portIdx + 1);
      }
    }

    SocketAddress inetSocketAddress = context.request().localAddress();
    if (inetSocketAddress == null) {
      return null;
    }
    return String.valueOf(inetSocketAddress.port());
  }
}
