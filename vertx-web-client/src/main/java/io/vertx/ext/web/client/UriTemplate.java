package io.vertx.ext.web.client;

/**
 * Uri templates for Web Client. For templating only the path, check out the {@link PathTemplate}.
 * <p>
 * This object can be reused for multiple requests.
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public interface UriTemplate {
  /**
   * Use parameters and convert to string. This method doesn't mutate internal state of the object.
   *
   * @param parameters the uri parameters to use during the expansion
   * @return the expanded value
   * @throws IllegalArgumentException if a parameter is missing
   */
  String expand(UriParameters parameters);
}
