package io.vertx.ext.web.handler.impl.logger;

import static io.vertx.ext.web.handler.impl.LoggerHandlerImpl.CONTEXT_REQUEST_START_PARAM;

import io.vertx.ext.web.RoutingContext;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeParameter extends BaseParameter {

  /**
   * Default time-format. 18/Sep/2011:19:18:28 -0400
   */
  private final static String DEFAULT_TIME_FORMAT = "%d/%b/%Y:%k:%M:%S %z";

  /**
   * date format as strftime
   */
  private final Strftime accessLogFmt;

  public TimeParameter(String parParam, String timeZoneId) {
    super(parParam);
    accessLogFmt = new Strftime(parParam == null ? DEFAULT_TIME_FORMAT : parParam, Locale.ENGLISH);
    if (timeZoneId != null) {
      accessLogFmt.setTimeZone(TimeZone.getTimeZone(timeZoneId));
    }
  }

  @Override
  protected String getValue(RoutingContext context) {
    return accessLogFmt.format(getRequestStartDate(context));
  }

  private Date getRequestStartDate(RoutingContext context) {
    long timestamp = context.get(CONTEXT_REQUEST_START_PARAM);
    return new Date(timestamp);
  }
}
