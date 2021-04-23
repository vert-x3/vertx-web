package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.UriParametersImpl;

import java.util.List;
import java.util.Map;

/**
 * Container for uri parameters to be used with {@link UriTemplate} or {@link PathTemplate}
 */
@VertxGen
public interface UriParameters {

  /**
   * Like {@link UriParameters#param(String, String)}
   */
  @GenIgnore
  default UriParameters param(String key, Object value) {
    return param(key, value.toString());
  }

  /**
   * Add param. This param will be escaped with {@link java.net.URLEncoder#encode(String, String)}
   *
   * @param key   parameter key
   * @param value parameter value
   * @return this
   */
  UriParameters param(String key, String value);

  /**
   * Add array param. This param will be escaped with {@link java.net.URLEncoder#encode(String, String)} <br/>
   * <p>
   * This param will be rendered in path as {@code value1/value2}
   *
   * @param key   parameter key
   * @param value parameter value
   * @return this
   */
  UriParameters param(String key, List<String> value);

  /**
   * Add param already escaped. This param won't be escaped as with {@link this#param(String, String)}
   *
   * @param key   parameter key
   * @param value parameter value already escaped
   * @return this
   */
  UriParameters escapedParam(String key, String value);

  /**
   * Add array param already escaped. This param won't be escaped as with {@link this#param(String, List)} <br/>
   * <p>
   * This param will be rendered in path as {@code value1/value2}
   *
   * @param key   parameter key
   * @param value parameter value already escaped
   * @return this
   */
  UriParameters escapedParam(String key, List<String> value);

  /**
   * Get parameter already escaped
   *
   * @param key the key of the parameter to get
   * @return the parameter, if any
   */
  List<String> getEscapedParam(String key);

  /**
   * @return a new empty {@link UriParameters}
   */
  static UriParameters create() {
    return new UriParametersImpl();
  }

  /**
   * Create {@link UriParameters} starting from a map.
   *
   * @param map the map with unescaped values
   * @return a new {@link UriParameters} containing the content of the provided map
   */
  static UriParameters fromMap(Map<String, Object> map) {
    UriParameters params = UriParameters.create();
    map.forEach(params::param);
    return params;
  }

}
