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
public interface DesignDrivenRouterFactory<Specification> {

  /**
   * Mount to paths that have to follow a security schema a security handler
   *
   * @param securitySchemaName
   * @param handler
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory addSecurityHandler(String securitySchemaName, Handler<RoutingContext> handler);

  /**
   * Add an handler to a path with a method. If combination path/method is not available in
   * specification, it will throw a {@link RouterFactoryException}
   *
   * @param method
   * @param path
   * @param handler
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory addHandler(HttpMethod method, String path, Handler<RoutingContext> handler);

  /**
   * Add a failure handler to a path with a method. If combination path/method is not available in
   * specification, it will throw a {@link RouterFactoryException}
   *
   * @param method
   * @param path
   * @param failureHandler
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory addFailureHandler(HttpMethod method, String path, Handler<RoutingContext> failureHandler);

  /**
   * Set default validation failure handler. You can disable this feature from
   * {@link DesignDrivenRouterFactory#enableValidationFailureHandler(boolean)}
   *
   * @param handler
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory setValidationFailureHandler(Handler<RoutingContext> handler);

  /**
   * Enable or disable validation failure handler. If you enable it, during router creation it will be mounted a
   * built-in (or custom with function {@link DesignDrivenRouterFactory#setValidationFailureHandler(Handler)})
   * ValidationException handler as a failure handler. If failure is different from ValidationException, it will be
   * called the next failure handler.
   *
   * @param enable
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory enableValidationFailureHandler(boolean enable);

  /**
   * Automatic mount handlers that return HTTP 501 status code for operations where you didn't specify an handler.
   *
   * @param enable
   * @return
   */
  @Fluent
  DesignDrivenRouterFactory mountOperationsWithoutHandlers(boolean enable);

  /**
   * Construct a new router based on spec. It will fail if you are trying to mount a spec with security schemes
   * without assigned handlers<br/>
   * <b>Note:</b> Router is constructed in this function, so it will be respected the path definition ordering.
   *
   * @return
   */
  Router getRouter();

}
