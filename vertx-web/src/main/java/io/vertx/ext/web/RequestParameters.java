package io.vertx.ext.web;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * Container for request parameters
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameters {

  /**
   * Get list of all parameter names inside path
   *
   * @return
   */
  List<String> getPathParametersNames();

  /**
   * Get path parameter by name
   * @param name Parameter name
   * @return
   */
  @Nullable
  RequestParameter getPathParameter(String name);

  /**
   * Get list of all parameter names inside query
   * @return
   */
  List<String> getQueryParametersNames();

  /**
   * Get query parameter by name
   * @param name Parameter name
   * @return
   */
  @Nullable
  RequestParameter getQueryParameter(String name);

  /**
   * Get list of all parameter names inside header
   * @return
   */
  List<String> getHeaderParametersNames();

  /**
   * Get header parameter by name
   * @param name Parameter name
   * @return
   */
  @Nullable
  RequestParameter getHeaderParameter(String name);

  /**
   * Get list of all parameter names inside cookie
   * @return
   */
  List<String> getCookieParametersNames();

  /**
   * Get cookie parameter by name
   * @param name Parameter name
   * @return
   */
  @Nullable
  RequestParameter getCookieParameter(String name);

  /**
   * Get list of all parameter names inside body form
   * @return
   */
  List<String> getFormParametersNames();

  /**
   * Get form parameter by name
   * @param name Parameter name
   * @return
   */
  @Nullable
  RequestParameter getFormParameter(String name);

  /**
   * Return request body
   * @return
   */
  @Nullable
  RequestParameter getBody();

}
