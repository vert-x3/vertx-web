package io.vertx.ext.web.client.checks;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.NoStackTraceThrowable;

class StatusCheck implements ResponsePredicate {

  private final int min;
  private final int max;

  StatusCheck(int min, int max) {
    if (min >= max) {
      throw new IllegalStateException();
    }
    this.min = min;
    this.max = max;
  }

  StatusCheck(int code) {
    this(code, code + 1);
  }

  @Override
  public boolean test(HttpClientResponse response) {
    int sc = response.statusCode();
    return sc >= min && sc < max;
  }

  @Override
  public Throwable mapToError(HttpClientResponse response) {
    int sc = response.statusCode();
    if (max - min == 1) {
      return new NoStackTraceThrowable("Response status code " + sc +  " is not equals to " + min);
    } else {
      return new NoStackTraceThrowable("Response status code " + sc +  " is not between " + min + " and " + max);
    }
  }
}
