package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class TimeParameter extends BaseParameter {

  /**
   * Default time-format. 18/Sep/2011:19:18:28 -0400
   */
  private final static String DEFAULT_TIME_FORMAT = "%d/%b/%Y:%k:%M:%S %z";

  /**
   * date format as strftime
   */
  private final Strftime accessLogFmt;

  public TimeParameter(String parParam, TimeZone timeZone) {
    super(parParam);
    accessLogFmt = new Strftime(parParam == null ? DEFAULT_TIME_FORMAT : parParam, Locale.ENGLISH);
    if (timeZone != null) {
      accessLogFmt.setTimeZone(timeZone);
    }
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return accessLogFmt.format(getRequestStartDate(context));
  }

  private Date getRequestStartDate(RoutingContext context) {
    long timestamp = context.get("logger-requestStart");
    return new Date(timestamp);
  }
}
