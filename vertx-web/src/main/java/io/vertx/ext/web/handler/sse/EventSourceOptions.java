package io.vertx.ext.web.handler.sse;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;

@DataObject(generateConverter = true, publicConverter = false)
public class EventSourceOptions extends HttpClientOptions {

  public final static long DEFAULT_RETRY_PERIOD = 60000;

  private long retryPeriod;

  public EventSourceOptions() {
    super();
    retryPeriod = DEFAULT_RETRY_PERIOD;
  }

  public EventSourceOptions(HttpClientOptions clientOptions) {
    super(clientOptions);
    this.retryPeriod = DEFAULT_RETRY_PERIOD;
  }

  public EventSourceOptions(EventSourceOptions other) {
    super(other);
    this.retryPeriod = other.getRetryPeriod();
  }

  public EventSourceOptions setRetryPeriod(long retryPeriod) {
    this.retryPeriod = retryPeriod;
    return this;
  }

  public long getRetryPeriod() {
    return retryPeriod;
  }

}
