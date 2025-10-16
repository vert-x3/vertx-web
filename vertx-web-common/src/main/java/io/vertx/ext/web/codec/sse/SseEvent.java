package io.vertx.ext.web.codec.sse;

import io.vertx.codegen.annotations.DataObject;

/**
 * This represents a Server-Sent Event.
 * @see https://html.spec.whatwg.org/multipage/server-sent-events.html
 */
@DataObject
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

  /**
   * Returns the event id.
   * @return the event id.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the type of the event.
   * @return the type of the event.
   */
  public String event() {
    return event;
  }

  /**
   * Returns the payload of the event.
   * @return the payload of the event.
   */
  public String data() {
    return data;
  }

  /**
   * Returns the reconnection time.
   * @return the reconnection time.
   */
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
    return "id: " + id + '\n'
        + "event: " + event + '\n'
        + "data: '" + data + '\n'
        + "retry: " + retry + '\n'
        + '\n';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String id;
    private String event = "message";
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

    void parseLine(String line) {
      int colonIndex = line.indexOf(':');
      if (colonIndex == 0) {
        return;
      }
      if (colonIndex == -1) {
        processField(line, "");
        return;
      }
      String field = line.substring(0, colonIndex);
      String value = line.substring(colonIndex + 1);
      // Remove leading space from value if present (SSE spec)
      if (value.startsWith(" ")) {
        value = value.substring(1);
      }
      processField(field, value);
    }

    private void processField(String field, String value) {
      // Field names must be compared literally, with no case folding performed.
      switch (field) {
        case "event":
          event(value);
          break;
        case "data":
          data(value);
          break;
        case "id":
          id(value);
          break;
        case "retry":
          // If the field value consists of only ASCII digits, then interpret the field value as an
          // integer in base ten, and set the event stream's reconnection time to that integer.
          // Otherwise, ignore the field.
          try {
            retry(Integer.parseInt(value));
          } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid \"retry\" value:" + value, ex);
          }
          break;
        default:
          // Ignore unknown fields as per SSE spec
          break;
      }
    }

    public SseEvent build() {
      return new SseEvent(this.id, this.event, this.data.toString(), this.retry);
    }
  }
}
