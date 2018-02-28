/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.impl;

import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.LoggerHandlerOptions;
import io.vertx.ext.web.handler.impl.logger.BaseParameter;
import io.vertx.ext.web.handler.impl.logger.ByteCountParameter;
import io.vertx.ext.web.handler.impl.logger.ClientIPParameter;
import io.vertx.ext.web.handler.impl.logger.CookieParameter;
import io.vertx.ext.web.handler.impl.logger.DurationParameter;
import io.vertx.ext.web.handler.impl.logger.EnvironmentVarParameter;
import io.vertx.ext.web.handler.impl.logger.FirstRequestLineParameter;
import io.vertx.ext.web.handler.impl.logger.HeaderParameter;
import io.vertx.ext.web.handler.impl.logger.LocalIPParameter;
import io.vertx.ext.web.handler.impl.logger.LocalPortParameter;
import io.vertx.ext.web.handler.impl.logger.MethodParameter;
import io.vertx.ext.web.handler.impl.logger.NonImplementedParameter;
import io.vertx.ext.web.handler.impl.logger.ParamParameter;
import io.vertx.ext.web.handler.impl.logger.Parameter;
import io.vertx.ext.web.handler.impl.logger.PlainTextParameter;
import io.vertx.ext.web.handler.impl.logger.ProtocolParameter;
import io.vertx.ext.web.handler.impl.logger.QueryParameter;
import io.vertx.ext.web.handler.impl.logger.ServerNameParameter;
import io.vertx.ext.web.handler.impl.logger.StatusParameter;
import io.vertx.ext.web.handler.impl.logger.ThreadParameter;
import io.vertx.ext.web.handler.impl.logger.TimeParameter;
import io.vertx.ext.web.handler.impl.logger.UrlPathParameter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * An access logger.
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="mailto:marcin.czeczko@gmail.com">Marcin Czeczko</a>
 */
public class LoggerHandlerImpl implements LoggerHandler {

  private final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(this.getClass());

  public final static String CONTEXT_REQUEST_DURATION_PARAM = "RequestDurationMs";

  public final static String CONTEXT_REQUEST_START_PARAM = "RequestStartEpoch";

  /**
   * The parsed list of log format parts whose <code>print</code> method is
   * called when building the log message line.
   */
  private List<Parameter> parameters;

  /**
   * A configuration options of the access log formatter.
   */
  private LoggerHandlerOptions options;

  public LoggerHandlerImpl(LoggerHandlerOptions options) {
    this.options = options;
    this.parameters = parse();
  }

  private void log(RoutingContext context, long timestamp) {

    context.put(CONTEXT_REQUEST_START_PARAM, timestamp);
    int status = context.response().getStatusCode();
    String message = format(context);

    if (status >= 500) {
      logger.error(message);
    } else if (status >= 400) {
      logger.warn(message);
    } else {
      logger.info(message);
    }
  }


  @Override
  public void handle(RoutingContext context) {
    long start = System.currentTimeMillis();

    context.addHeadersEndHandler(v -> context
      .put(CONTEXT_REQUEST_DURATION_PARAM,
        System.currentTimeMillis() - start));

    if (options.isImmediate()) {
      log(context, start);
    } else {
      context.addBodyEndHandler(v -> log(context, start));
    }

    context.next();

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
  private String format(RoutingContext context) {
    if (!parameters.isEmpty()) {
      return parameters.stream()
        .map(param -> param.print(context))
        .reduce(new StringBuilder(), StringBuilder::append)
        .toString();
    }
    return "";
  }

  private List<Parameter> parse() {
    List<Parameter> parameterList = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();

    CharacterIterator charIt = new StringCharacterIterator(options.getPattern());

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
        param = new ByteCountParameter(options.isImmediate());
        break;

      case 'C':
        param = (name == null) ? null : new CookieParameter(name);
        break;

      case 'D':
        param = new DurationParameter();
        break;

      case 'e':
        param = (name == null) ? null : new EnvironmentVarParameter(name);
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
        param = (name == null) ? null : new ParamParameter(name);
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
        param = new TimeParameter(name, options.getTimeZoneID());
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
