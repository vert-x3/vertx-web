package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Options for configuring the Logger Handler.
 *
 * @author <a href="mailto:marcin.czeczko@gmail.com">Marcin Czeczko</a>
 */
@DataObject
public class LoggerHandlerOptions {

  /**
   * Default log pattern that follows NCSA extended/combined log format
   * <i>remote-client</i> - - [<i>timestamp</i>] "<i>method</i> <i>uri</i> <i>version</i>" <i>status</i> <i>content-length</i> "<i>referrer</i>" "<i>user-agent</i>"
   */
  String DEFAULT_PATTERN = "%h - - [%t] \"%r\" %s %b \"%{Referer}i\" \"%{User-agent}i\"]";

  private String pattern;

  private String timeZoneID;

  private boolean immediate;

  /**
   * Copy constructor
   *
   * @param other the options to copy
   */
  public LoggerHandlerOptions(LoggerHandlerOptions other) {
    this.pattern = other.pattern;
    this.timeZoneID = other.getTimeZoneID();
    this.immediate = other.immediate;
  }

  /**
   * Default constructor
   */
  public LoggerHandlerOptions() {
    this.timeZoneID = null;
    this.pattern = DEFAULT_PATTERN;
    this.immediate = false;
  }

  /**
   * Constructor from JSON
   *
   * @param json the JSON
   */
  public LoggerHandlerOptions(JsonObject json) {
    this.timeZoneID = json.getString("timeZoneID");
    this.pattern = json.getString("pattern");
    this.immediate = json.getBoolean("immediate", false);
  }

  public String getTimeZoneID() {
    return timeZoneID;
  }

  /**
   * A timeZone to be used to printout timestamp parameters. If not set, a default local timeZone is being used.
   *
   * @param timeZoneID a string with the ID of the timeZone.
   * @return a reference to this, so the API can be used fluently
   * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html">https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html</a>
   */
  public LoggerHandlerOptions setTimeZoneID(String timeZoneID) {
    this.timeZoneID = timeZoneID;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  /**
   * A apache format log pattern
   *
   * @param pattern a log line pattern
   * @return a reference to this, so the API can be used fluently
   */
  public LoggerHandlerOptions setPattern(String pattern) {
    this.pattern = pattern;
    return this;
  }

  public boolean isImmediate() {
    return immediate;
  }

  /**
   * Whether logging should occur as soon as request arrives or not
   *
   * @param immediate true if logging should occur as soon as request arrives
   * @return a reference to this, so the API can be used fluently
   */
  public LoggerHandlerOptions setImmediate(boolean immediate) {
    this.immediate = immediate;
    return this;
  }
}
