package io.vertx.ext.web.codec;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Unstable;
import io.vertx.core.json.JsonObject;

/**
 * This represents a Server-Sent Event.
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html">Server-sent events</a>
 */
@Unstable
@DataObject
public class SseEvent {

  private String id;
  private String event;
  private String data;
  private int retry;

   public SseEvent(){

   }

  public SseEvent(String id, String event, String data, int retry) {
    this.id = id;
    this.event = event;
    this.data = data;
    this.retry = retry;
  }

  public SseEvent(JsonObject json) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "id":
          if (member.getValue() instanceof String) {
            this.id = (String) member.getValue();
          }
          break;
        case "event":
          if (member.getValue() instanceof String) {
            this.event = (String) member.getValue();
          }
          break;
        case "data":
          if (member.getValue() instanceof String) {
            this.data = (String) member.getValue();
          }
          break;
        case "retry":
          if (member.getValue() instanceof Number) {
            this.retry = ((Number)member.getValue()).intValue();
          }
          break;
      }
    }
  }

  public SseEvent(SseEvent other) {
    this.id = other.id();
    this.event = other.event();
    this.data = other.data();
    this.retry = other.retry();
  }

  /**
   * Returns the event id.
   *
   * @return the event id.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the type of the event.
   *
   * @return the type of the event.
   */
  public String event() {
    return event;
  }

  /**
   * Returns the payload of the event.
   *
   * @return the payload of the event.
   */
  public String data() {
    return data;
  }

  /**
   * Returns the reconnection time.
   *
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
