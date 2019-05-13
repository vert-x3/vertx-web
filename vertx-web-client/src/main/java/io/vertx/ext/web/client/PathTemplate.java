package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.PathTemplateImpl;

/**
 * Path templates for Web Client. Supports the same syntax of Vert.x Web path templates:
 * You can define parameters placeholders with {@code :} followed by the parameter name. For instance:<br/>
 * <ul>
 *     <li>{@code "/:name/:id?hello=name"} with parameters (name: francesco),(id: 10) will translate to {@code /francesco/10?hello=name}</li>
 *     <li>{@code "/api/v=:api_version/users/:id"} with parameters (api_version: 1),(id: 10) will translate to {@code /api/v=1/users/10}</li>
 * </ul>
 *
 * This object can be reused for multiple requests
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface PathTemplate {

  /**
   * Use parameters and convert to string. This method doesn't mutate internal state of the object
   *
   * @param parameters
   * @throws IllegalArgumentException if a parameter is missing
   * @return
   */
  String expand(PathParameters parameters);

  /**
   * Parse a path template
   *
   * @param s
   * @throws IllegalArgumentException If the provided string is not a valid path template
   * @return
   */
  static PathTemplate parse(String s) {
    return PathTemplateImpl.parse(s);
  }

}
