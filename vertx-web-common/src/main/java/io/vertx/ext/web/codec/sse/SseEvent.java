package io.vertx.ext.web.codec.sse;

public class SseEvent {

  private final String id;
  private final String event;
  private final String data;
  private final int retry;

  public SseEvent(String id, String event, String data, int retry) {
    this.id = id;
    this.event = event;
    this.data = data;
    this.retry = retry;
  }

  public String id() {
    return id;
  }

  public String event() {
    return event;
  }

  public String data() {
    return data;
  }

  public int retry() {
    return retry;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SseEvent sseEvent = (SseEvent) o;
    return retry == sseEvent.retry
        && java.util.Objects.equals(id, sseEvent.id)
        && java.util.Objects.equals(event, sseEvent.event)
        && java.util.Objects.equals(data, sseEvent.data);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, event, data, retry);
  }

  @Override
  public String toString() {
    return "SseEvent{"
        + "id='" + id + '\''
        + ", event='" + event + '\''
        + ", data='" + data + '\''
        + ", retry=" + retry
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String id;
    private String event;
    private StringBuilder data = new StringBuilder();
    private int retry;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder event(String event) {
      this.event = event;
      return this;
    }

    public Builder data(String data) {
      this.data.append(data);
      return this;
    }

    public Builder retry(int retry) {
      this.retry = retry;
      return this;
    }

    public SseEvent build() {
      return new SseEvent(this.id, this.event, this.data.toString(), this.retry);
    }
  }
}
