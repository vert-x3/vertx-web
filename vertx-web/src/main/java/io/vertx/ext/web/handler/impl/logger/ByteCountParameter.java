package io.vertx.ext.web.handler.impl.logger;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.ext.web.RoutingContext;

public class ByteCountParameter extends BaseParameter {

  private final boolean immediate;

  public ByteCountParameter(boolean immediate) {
    super(null);
    this.immediate = immediate;
  }

  @Override
  protected String getValue(RoutingContext context) {
    long contentLength = 0;
    if (immediate) {
      Object obj = context.request().headers().get(HttpHeaderNames.CONTENT_LENGTH);
      if (obj != null) {
        try {
          contentLength = Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
          // ignore it and continue
          contentLength = 0;
        }
      }
    } else {
      contentLength = context.response().bytesWritten();
    }

    if (contentLength == 0) {
      return (getParName() == 'b') ? "-" : "0";
    }
    return String.valueOf(contentLength);
  }
}
