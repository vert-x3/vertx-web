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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.impl.ParsableMIMEValue;
import io.vertx.ext.web.impl.Utils;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ErrorHandlerImpl implements ErrorHandler {

  private static final List<MIMEHeader> ERROR_MIMES = Arrays.asList(
        new ParsableMIMEValue("text/html"),
        new ParsableMIMEValue("application/json"),
        new ParsableMIMEValue("text/plain")
      );
  
  /**
   * Flag to enable/disable printing the full stack trace of exceptions.
   */
  private final boolean displayExceptionDetails;

  /**
   * Cached template for rendering the html errors
   */
  private final String errorTemplate;

  public ErrorHandlerImpl(String errorTemplateName, boolean displayExceptionDetails) {
    Objects.requireNonNull(errorTemplateName);
    this.displayExceptionDetails = displayExceptionDetails;
    this.errorTemplate = Utils.readResourceToBuffer(errorTemplateName).toString();
  }

  @Override
  public void handle(RoutingContext context) {

    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    if (context.statusCode() != -1) {
      response.setStatusCode(context.statusCode());
    } else {
      // Internal error
      response.setStatusCode(500);
    }

    // does the response already set the mime type?
    String mime = response.headers().get(HttpHeaders.CONTENT_TYPE);

    int errorCode;
    String errorMessage = null;
    if (context.statusCode() != -1) {
      errorCode = context.statusCode();
      errorMessage = context.response().getStatusMessage();
    } else {
      errorCode = 500;
      if (displayExceptionDetails) {
        errorMessage = context.failure().getMessage();
      }
      if (errorMessage == null) {
        errorMessage = "Internal Server Error";
      }
      // no new lines are allowed in the status message
      response.setStatusMessage(errorMessage.replaceAll("\\r|\\n", " "));
    }

    if (mime != null) {
      if (sendError(context, mime, errorCode, errorMessage)) {
        return;
      }
    }
    
    // respect the client accept order
    List<MIMEHeader> acceptableMimes = context.parsedHeaders().accept();

    for (MIMEHeader accept : acceptableMimes) {
      Optional<MIMEHeader> matchedHeader = accept.findMatchedBy(ERROR_MIMES);
      if (matchedHeader.isPresent()) {
        sendError(context, matchedHeader.get().value(), errorCode, errorMessage);
        return;
      }
    }

    // fall back plain/text
    sendError(context, "text/plain", errorCode, errorMessage);
  }

  private boolean sendError(RoutingContext context, String mime, int errorCode, String errorMessage) {

    final String title = "Matron!";

    HttpServerResponse response = context.response();

    if (mime.startsWith("text/html")) {
      StringBuilder stack = new StringBuilder();
      if (context.failure() != null && displayExceptionDetails) {
        for (StackTraceElement elem: context.failure().getStackTrace()) {
          stack.append("<li>").append(elem).append("</li>");
        }
      }
      response.putHeader(HttpHeaders.CONTENT_TYPE,"text/html");
      response.end(
        errorTemplate.replace("{title}", title)
          .replace("{errorCode}", Integer.toString(errorCode))
          .replace("{errorMessage}", errorMessage)
          .replace("{stackTrace}", stack.toString())
      );
      return true;
    }

    if (mime.startsWith("application/json")) {
      JsonObject jsonError = new JsonObject();
      jsonError.put("error", new JsonObject().put("code", errorCode).put("message", errorMessage));
      if (context.failure() != null && displayExceptionDetails) {
        JsonArray stack = new JsonArray();
        for (StackTraceElement elem: context.failure().getStackTrace()) {
          stack.add(elem.toString());
        }
        jsonError.put("stack", stack);
      }
      response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      response.end(jsonError.encode());
      return true;
    }

    if (mime.startsWith("text/plain")) {
      response.putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
      StringBuilder sb = new StringBuilder();
      sb.append("Error ");
      sb.append(errorCode);
      sb.append(": ");
      sb.append(errorMessage);
      if (context.failure() != null && displayExceptionDetails) {
        for (StackTraceElement elem: context.failure().getStackTrace()) {
          sb.append("\tat ").append(elem).append("\n");
        }
      }
      response.end(sb.toString());
      return true;
    }

    return false;
  }

}
