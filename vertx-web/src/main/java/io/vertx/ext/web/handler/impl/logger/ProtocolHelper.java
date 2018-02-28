package io.vertx.ext.web.handler.impl.logger;

import io.vertx.core.http.HttpVersion;

class ProtocolHelper {

  public static String toString(HttpVersion httpVersion) {
    String versionFormatted = null;
    switch (httpVersion) {
      case HTTP_1_0:
        versionFormatted = "HTTP/1.0";
        break;
      case HTTP_1_1:
        versionFormatted = "HTTP/1.1";
        break;
      case HTTP_2:
        versionFormatted = "HTTP/2.0";
        break;
    }

    return versionFormatted;
  }

}
