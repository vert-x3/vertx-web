package io.vertx.ext.web;

import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameters {

  List<String> getPathParametersNames();

  RequestParameter getPathParameter(String name);

  List<String> getQueryParametersNames();

  RequestParameter getQueryParameter(String name);

  List<String> getHeaderParametersNames();

  RequestParameter getHeaderParameter(String name);

  List<String> getCookieParametersNames();

  RequestParameter getCookieParameter(String name);

  List<String> getFormParametersNames();

  RequestParameter getFormParameter(String name);

}
