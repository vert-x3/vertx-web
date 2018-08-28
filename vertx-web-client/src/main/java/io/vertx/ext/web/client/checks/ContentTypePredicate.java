package io.vertx.ext.web.client.checks;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.Collections;
import java.util.List;

class ContentTypePredicate implements ResponsePredicate {

  private final List<String> mimeTypes;

  public ContentTypePredicate(List<String> mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  public ContentTypePredicate(String mimeType) {
    this(Collections.singletonList(mimeType));
  }

  @Override
  public boolean test(HttpClientResponse response) {
    String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType != null) {
      for (String mimeType : mimeTypes) {
        if (mimeType.equals(contentType)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Throwable mapToError(HttpClientResponse response) {
    String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
    String msg;
    if (contentType == null) {
      msg = "Missing response content type";
    } else {
      StringBuilder sb = new StringBuilder("Expect content type ").append(contentType).append(" to be one of ");
      boolean first = true;
      for (String mimeType : mimeTypes) {
        if (!first) {
          sb.append(", ");
        }
        first = false;
        sb.append(mimeType);
      }
      msg = sb.toString();
    }
    return new NoStackTraceThrowable(msg);
  }
}
