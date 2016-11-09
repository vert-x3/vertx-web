package io.vertx.ext.web.impl;

import io.vertx.core.VertxException;

public class HeaderTooLongException extends VertxException {

  public HeaderTooLongException(String reason) {
    super(reason);
  }
  
}