package io.vertx.ext.web.handler;

/*
 * A utility exception class that represent failures while trying to validate CSRF Token
 * */
public class CSRFValidationException extends RuntimeException {

  public CSRFValidationException(String message){
    super(message, null);
  }

}
