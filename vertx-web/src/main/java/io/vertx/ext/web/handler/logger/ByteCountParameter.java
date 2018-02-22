package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class ByteCountParameter extends BaseParameter {

  public ByteCountParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    long contentLength = 0;
    if (immediate) {
      Object obj = context.request().headers().get("content-length");
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
