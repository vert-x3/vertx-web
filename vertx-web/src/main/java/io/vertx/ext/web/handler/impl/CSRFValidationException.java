package io.vertx.ext.web.handler.impl;

/*
 * A utility exception class that represent CSRF validation failures
 * */
public class CSRFValidationException extends AccessDeniedException {

  public CSRFValidationException(String message){
    super(message);
  }

}
