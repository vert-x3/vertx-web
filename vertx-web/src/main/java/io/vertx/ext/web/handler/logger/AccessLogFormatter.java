package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AccessLogFormatter {

  /**
   * The parsed list of log format parts whose <code>print</code> method is
   * called when building the log message line.
   */
  private List<Parameter> parameters;

  /**
   * A current selected timeZone in which a date/time should be formatted.
   */
  private TimeZone timeZone;

  /**
   * Creates a new instance from of this class parsing the log format pattern.
   *
   * @param pattern The pattern to be parsed.
   */
  public AccessLogFormatter(String pattern) {
    this.parameters = parse(pattern);
  }

  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * Creates a log message from the given <code>routingContext</code> objects
   * according to the log format from which this instance has been created.
   *
   * @param context The {@link RoutingContext} used to extract values
   * for the log message.
   * @return The formatted log message or <code>null</code> if this log
   * formatter has not been initialized with a valid log format
   * pattern.
   */
  public String format(RoutingContext context, boolean immediate) {
    if (!parameters.isEmpty()) {
      return parameters.stream()
        .map(param -> param.print(context, immediate))
        .reduce(new StringBuilder(), StringBuilder::append)
        .toString();
    }
    return null;
  }

  private List<Parameter> parse(String pattern) {
    List<Parameter> parameterList = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();

    CharacterIterator charIt = new StringCharacterIterator(pattern);

    for (int currentChar = charIt.first(); currentChar != CharacterIterator.DONE;
      currentChar = charIt.next()) {
      if (currentChar == '%') {
        int nextChar = charIt.next();
        if (nextChar != '%') {
          if (buffer.length() > 0) {
            Parameter text = new PlainTextParameter(buffer.toString());
            parameterList.add(text);
            buffer.setLength(0);
          }

          Parameter param = this.parseFormatString(charIt, nextChar);
          if (param != null) {
            parameterList.add(param);
          }
          continue;
        }
      }

      buffer.append((char) currentChar);
    }

    // append any remaining plain text
    if (buffer.length() > 0) {
      Parameter text = new PlainTextParameter(buffer.toString());
      parameterList.add(text);
      buffer.setLength(0);
    }

    return parameterList;
  }

  private Parameter parseFormatString(CharacterIterator charIt, int currentChar) {

    // read all modifiers
    boolean required = true;
    int[] statCodes = null;
    while (currentChar != CharacterIterator.DONE) {
      if (currentChar == '!') {
        required = false;
      } else if (currentChar >= '0' && currentChar <= '9') {
        statCodes = this.parseStatusCodes(charIt, currentChar);
      } else if (currentChar == '>' || currentChar == '<') {
        // ignore first/last modifiers
      } else {
        break;
      }

      currentChar = charIt.next();
    }

    // read name
    String name;
    if (currentChar == '{') {
      StringBuilder nameBuf = new StringBuilder();
      for (currentChar = charIt.next(); currentChar != CharacterIterator.DONE && currentChar != '}';
        currentChar = charIt.next()) {
        nameBuf.append((char) currentChar);
      }
      name = (nameBuf.length() > 0) ? nameBuf.toString() : null;

      // get the format indicator
      currentChar = charIt.next();
    } else {
      name = null;
    }

    Parameter param;
    switch (currentChar) {
      case 'a':
      case 'h': //remote host parameter - no hostname lookups so far
        param = new ClientIPParameter();
        break;

      case 'A':
        param = new LocalIPParameter();
        break;

      case 'b':
      case 'B':
        param = new ByteCountParameter();
        break;

      case 'C':
        param = (name == null) ? null : new CookieParameter(name);
        break;

      case 'D':
        param = new DurationParameter();
        break;

      case 'e':
        param = new EnvironmentVarParameter(name);
        break;

      case 'H':
        param = new ProtocolParameter();
        break;

      case 'i':
        param = (name == null) ? null : new HeaderParameter(name, true);
        break;

      case 'm':
        param = new MethodParameter();
        break;

      case 'M':
        param = new ParamParameter(name);
        break;

      case 'o':
        param = (name == null) ? null : new HeaderParameter(name, false);
        break;

      case 'p':
        param = new LocalPortParameter();
        break;

      case 'P':
        param = new ThreadParameter(name);
        break;

      case 'q':
        param = new QueryParameter();
        break;

      case 'r':
        param = new FirstRequestLineParameter();
        break;

      case 's':
        param = new StatusParameter();
        break;

      case 't':
        param = new TimeParameter(name, timeZone);
        break;

      case 'T':
        param = new DurationParameter();
        break;

      case 'U':
        param = new UrlPathParameter();
        break;

      case 'v':
      case 'V':
        param = new ServerNameParameter();
        break;

      // other options that's not supported fall through to default
      default:
        param = new NonImplementedParameter(name);
        break;
    }

    if (param instanceof BaseParameter) {
      BaseParameter baseParam = (BaseParameter) param;
      baseParam.setParName((char) currentChar);
      baseParam.setRequired(required);
      baseParam.setStatusLimits(statCodes);
    }

    return param;
  }

  private int[] parseStatusCodes(CharacterIterator charIt, int currentChar) {
    StringBuilder buf = new StringBuilder();
    buf.append((char) currentChar);

    List<Integer> numbers = new ArrayList<>();
    for (currentChar = charIt.next(); currentChar != CharacterIterator.DONE;
      currentChar = charIt.next()) {
      if (currentChar == ',') {
        int num = 0;
        try {
          num = Integer.parseInt(buf.toString());
        } catch (NumberFormatException nfe) {
          // don't care
        }
        if (num >= 100 && num <= 999) {
          numbers.add(num);
        }
        buf.setLength(0);
      } else if (currentChar >= '0' && currentChar <= '9') {
        buf.append((char) currentChar);
      } else {
        // end of number list
        break;
      }
    }

    // reset to the last mark
    charIt.previous();

    // get the last number
    int num = 0;
    try {
      num = Integer.parseInt(buf.toString());
    } catch (NumberFormatException nfe) {
      // don't care
    }
    if (num >= 100 && num <= 999) {
      numbers.add(new Integer(num));
    }

    if (numbers.isEmpty()) {
      return null;
    }

    int[] statusCodes = new int[numbers.size()];
    for (int i = 0; i < numbers.size(); i++) {
      statusCodes[i] = (numbers.get(i)).intValue();
    }
    return statusCodes;
  }
}
