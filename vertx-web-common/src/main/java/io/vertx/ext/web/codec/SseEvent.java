package io.vertx.ext.web.codec;

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

}
