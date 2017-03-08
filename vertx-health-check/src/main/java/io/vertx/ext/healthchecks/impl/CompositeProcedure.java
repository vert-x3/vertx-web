package io.vertx.ext.healthchecks.impl;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface CompositeProcedure extends Procedure {

  CompositeProcedure add(String name, Procedure check);

  boolean remove(String name);

  Procedure get(String name);
}
