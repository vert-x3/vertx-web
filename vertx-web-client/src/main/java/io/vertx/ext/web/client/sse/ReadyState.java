package io.vertx.ext.web.client.sse;

/**
 * The state of the connection.
 */
public enum ReadyState {
  /** The connection has not yet been established, or it was closed and the user agent is reconnecting. */
  CONNECTING(0),
  /** The user agent has an open connection and is dispatching events as it receives them. */
  OPEN(1),
  /**
   * The connection is not open, and the user agent is not trying to reconnect.
   * <p>
   * Either there was a fatal error or the close() method was invoked.
   */
  CLOSED(2);

  private final int numericValue;

  ReadyState(int value) {
    this.numericValue = value;
  }

  public int getNumericValue() {
    return numericValue;
  }
}
