package io.vertx.ext.healthchecks.impl;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ProcedureException extends Exception {

  public ProcedureException(String msg) {
    super(msg);
  }

  public ProcedureException(Exception cause) {
    super(cause.getMessage(), cause);
  }

}
