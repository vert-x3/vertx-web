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

import java.util.List;
import java.util.Objects;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.impl.Utils;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ErrorHandlerImpl implements ErrorHandler {

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

    HttpServerResponse response = context.response();

    Throwable failure = context.failure();

    int errorCode = context.statusCode();
    String errorMessage = null;
    if (errorCode != -1) {
      context.response().setStatusCode(errorCode);
      errorMessage = context.response().getStatusMessage();
    } else {
      errorCode = 500;
      if (displayExceptionDetails) {
        errorMessage = failure.getMessage();
      }
      if (errorMessage == null) {
        errorMessage = "Internal Server Error";
      }
      // no new lines are allowed in the status message
      response.setStatusMessage(errorMessage.replaceAll("\\r|\\n", " "));
    }

    answerWithError(context, errorCode, errorMessage);
  }

  private void answerWithError(RoutingContext context, int errorCode, String errorMessage){
    context.response().setStatusCode(errorCode);
    if( !sendErrorResponseMIME(context, errorCode, errorMessage) &&
        !sendErrorAcceptMIME(context, errorCode, errorMessage)
      ){
      // fallback plain/text
      sendError(context, "text/plain", errorCode, errorMessage);
    }
  }

  private boolean sendErrorResponseMIME(RoutingContext context, int errorCode, String errorMessage){
    // does the response already set the mime type?
    String mime = context.response().headers().get(HttpHeaders.CONTENT_TYPE);

    return mime != null && sendError(context, mime, errorCode, errorMessage);
  }

  private boolean sendErrorAcceptMIME(RoutingContext context, int errorCode, String errorMessage){
    // respect the client accept order
    List<MIMEHeader> acceptableMimes = context.parsedHeaders().accept();

    for (MIMEHeader accept : acceptableMimes) {
      if (sendError(context, accept.value(), errorCode, errorMessage)) {
        return true;
      }
    }
    return false;
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
