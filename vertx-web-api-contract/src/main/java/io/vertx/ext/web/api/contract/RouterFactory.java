package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Main interface for Design Driven Router factory
 * Author: Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface RouterFactory<Specification> {

  /**
   * Mount to paths that have to follow a security schema a security handler
   *
   * @param securitySchemaName
   * @param handler
   * @return
   */
  @Fluent
  RouterFactory addSecurityHandler(String securitySchemaName, Handler<RoutingContext> handler);

  /**
   * Add an handler to a path with a method. If combination path/method is not available in
   * specification, it will throw a {@link RouterFactoryException}. Deprecated in favour of
   * operation id
   *
   * @param method
   * @param path
   * @param handler
   * @return
   */
  @Fluent @Deprecated
  RouterFactory addHandler(HttpMethod method, String path, Handler<RoutingContext> handler);

  /**
   * Add a failure handler to a path with a method. If combination path/method is not available in
   * specification, it will throw a {@link RouterFactoryException}. Deprecated in favour of
   * operation id
   *
   * @param method
   * @param path
   * @param failureHandler
   * @return
   */
  @Fluent @Deprecated
  RouterFactory addFailureHandler(HttpMethod method, String path, Handler<RoutingContext> failureHandler);

  /**
   * Override options
   *
   * @param options
   * @return
   */
  @Fluent
  RouterFactory setOptions(RouterFactoryOptions options);

  /**
   * Get options of router factory. For more info {@link RouterFactoryOptions}
   *
   * @return
   */
  RouterFactoryOptions getOptions();

  /**
   * Deprecated. Instantiate {@link RouterFactoryOptions}
   * and load it using {@link RouterFactory#setOptions(RouterFactoryOptions)}
   *
   * @param handler
   * @return
   */
  @Fluent @Deprecated
  RouterFactory setValidationFailureHandler(Handler<RoutingContext> handler);

  /**
   * Deprecated. Instantiate {@link RouterFactoryOptions}
   * and load it using {@link RouterFactory#setOptions(RouterFactoryOptions)}
   *
   * @param enable
   * @return
   */
  @Fluent @Deprecated
  RouterFactory enableValidationFailureHandler(boolean enable);

  /**
   * Deprecated. Instantiate {@link RouterFactoryOptions}
   * and load it using {@link RouterFactory#setOptions(RouterFactoryOptions)}
   *
   * @param enable
   * @return
   */
  @Fluent @Deprecated
  RouterFactory mountOperationsWithoutHandlers(boolean enable);

  /**
   * Construct a new router based on spec. It will fail if you are trying to mount a spec with security schemes
   * without assigned handlers<br/>
   * <b>Note:</b> Router is constructed in this function, so it will be respected the path definition ordering.
   *
   * @return
   */
  Router getRouter();

}
