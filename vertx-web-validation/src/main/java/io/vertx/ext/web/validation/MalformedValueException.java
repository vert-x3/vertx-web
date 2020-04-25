package io.vertx.ext.web.validation;

import io.vertx.core.VertxException;

/**
 * Represents a malformed value that parser is unable to manage
 */
public class MalformedValueException extends VertxException {

  public MalformedValueException(String message) {
    super(message);
  }

  public MalformedValueException(Throwable cause) {
    super(cause);
  }

}
