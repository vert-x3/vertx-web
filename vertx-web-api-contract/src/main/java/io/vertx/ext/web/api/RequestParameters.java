package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Container for request parameters
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameters {

  /**
   * Get list of all parameter names inside path
   *
   * @return
   */
  List<String> pathParametersNames();

  /**
   * Get path parameter by name
   *
   * @param name Parameter name
   * @return
   */
  @Nullable RequestParameter pathParameter(String name);

  /**
   * Get list of all parameter names inside query
   *
   * @return
   */
  List<String> queryParametersNames();

  /**
   * Get query parameter by name
   *
   * @param name Parameter name
   * @return
   */
  @Nullable RequestParameter queryParameter(String name);

  /**
   * Get list of all parameter names inside header
   *
   * @return
   */
  List<String> headerParametersNames();

  /**
   * Get header parameter by name
   *
   * @param name Parameter name
   * @return
   */
  @Nullable RequestParameter headerParameter(String name);

  /**
   * Get list of all parameter names inside cookie
   *
   * @return
   */
  List<String> cookieParametersNames();

  /**
   * Get cookie parameter by name
   *
   * @param name Parameter name
   * @return
   */
  @Nullable RequestParameter cookieParameter(String name);

  /**
   * Get list of all parameter names inside body form
   *
   * @return
   */
  List<String> formParametersNames();

  /**
   * Get form parameter by name
   *
   * @param name Parameter name
   * @return
   */
  @Nullable RequestParameter formParameter(String name);

  /**
   * Return request body
   *
   * @return
   */
  @Nullable RequestParameter body();

  /**
   * This method converts RequestParameters in an unique JsonObject with 6 fields: cookie, path, query, header, form, body<br/>
   *
   * cookie, path, query, header, form are JsonObject where keys are param names and values are param values, while body depends on body's shape and may not exist
   *
   * @return
   */
  @CacheReturn JsonObject toJson();

}
