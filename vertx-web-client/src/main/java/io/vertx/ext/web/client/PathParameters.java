package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.PathParametersImpl;

import java.util.List;
import java.util.Map;

/**
 * Container for path parameters to be used with {@link PathTemplate}
 */
@VertxGen
public interface PathParameters {

  /**
   * Like {@link PathParameters#param(String, String)}
   */
  @GenIgnore
  default PathParameters param(String key, Object value) {
    return param(key, value.toString());
  }

  /**
   * Add param. This param will be escaped with {@link java.net.URLEncoder#encode(String, String)}
   *
   * @param key
   * @param value
   * @return
   */
  PathParameters param(String key, String value);

  /**
   * Add array param. This param will be escaped with {@link java.net.URLEncoder#encode(String, String)} <br/>
   *
   * This param will be rendered in path as {@code value1/value2}
   *
   * @param key
   * @param value
   * @return
   */
  PathParameters param(String key, List<String> value);

  /**
   * Add param already escaped. This param won't be escaped as with {@link this#param(String, String)}
   *
   * @param key
   * @param value
   * @return
   */
  PathParameters escapedParam(String key, String value);

  /**
   * Add array param already escaped. This param won't be escaped as with {@link this#param(String, List)} <br/>
   *
   * This param will be rendered in path as {@code value1/value2}
   *
   * @param key
   * @param value
   * @return
   */
  PathParameters escapedParam(String key, List<String> value);

  /**
   * Get parameter already escaped
   *
   * @param key
   * @return
   */
  List<String> getEscapedParam(String key);

  /**
   * Create a new empty {@link PathParameters}
   *
   * @return
   */
  static PathParameters create() {
    return new PathParametersImpl();
  }

  /**
   * Create {@link PathParameters} starting from a map.
   *
   * @param map
   * @return
   */
  static PathParameters fromMap(Map<String, Object> map) {
    PathParameters params = PathParameters.create();
    map.forEach(params::param);
    return params;
  }

}
