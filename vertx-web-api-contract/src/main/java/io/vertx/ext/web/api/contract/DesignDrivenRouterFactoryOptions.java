package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@DataObject(generateConverter = true, publicConverter = false)
public class DesignDrivenRouterFactoryOptions {

  /**
   * Default validation failure handler. When ValidationException occurs, It sends a response
   * with status code 400, status message "Bad Request" and error message as body
   */
  public final static Handler<RoutingContext> DEFAULT_VALIDATION_HANDLER = (routingContext -> {
    if (routingContext.failure() instanceof ValidationException) {
      routingContext
        .response()
        .setStatusCode(400)
        .setStatusMessage("Bad Request")
        .end(routingContext.failure().getMessage());
    } else routingContext.next();
  });

  /**
   * By default, DesignDrivenRouterFactory loads validation failure handler
   */
  public final static boolean DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER = true;

  /**
   * Default not implemented handler. It sends a response with status code 501,
   * status message "Not Implemented" and empty body
   */
  public final static Handler<RoutingContext> DEFAULT_NOT_IMPLEMENTED_HANDLER = (routingContext) -> {
    routingContext.response().setStatusCode(501).setStatusMessage("Not Implemented").end();
  };


  /**
   * By default, DesignDrivenRouterFactory mounts Not Implemented handler
   */
  public final static boolean DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER = true;

  /**
   * By default, DesignDrivenRouterFactory requires security handlers
   * to be defined while calling getRouter() or it will throw an Exception
   */
  public final static boolean DEFAULT_REQUIRE_SECURITY_HANDLERS = true;

  private Handler<RoutingContext> validationFailureHandler;
  private boolean mountValidationFailureHandler;
  private Handler<RoutingContext> notImplementedFailureHandler;
  private boolean mountNotImplementedHandler;
  private boolean requireSecurityHandlers;

  public DesignDrivenRouterFactoryOptions() {
    init();
  }

  public DesignDrivenRouterFactoryOptions(JsonObject json) {
    init();
    DesignDrivenRouterFactoryOptionsConverter.fromJson(json, this);
  }

  public DesignDrivenRouterFactoryOptions(DesignDrivenRouterFactoryOptions other) {
    this.validationFailureHandler = other.getValidationFailureHandler();
    this.mountValidationFailureHandler = other.isMountValidationFailureHandler();
    this.notImplementedFailureHandler = other.getNotImplementedFailureHandler();
    this.mountNotImplementedHandler = other.isMountNotImplementedHandler();
    this.requireSecurityHandlers = other.isRequireSecurityHandlers();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    DesignDrivenRouterFactoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.validationFailureHandler = DEFAULT_VALIDATION_HANDLER;
    this.mountValidationFailureHandler = DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER;
    this.notImplementedFailureHandler = DEFAULT_NOT_IMPLEMENTED_HANDLER;
    this.mountNotImplementedHandler = DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER;
    this.requireSecurityHandlers = DEFAULT_REQUIRE_SECURITY_HANDLERS;
  }

  public Handler<RoutingContext> getValidationFailureHandler() {
    return validationFailureHandler;
  }

  /**
   * Set default validation failure handler. You can enable/disable this feature from
   * {@link DesignDrivenRouterFactoryOptions#setMountValidationFailureHandler(boolean)}
   *
   * @param validationFailureHandler
   * @return this object
   */
  @Fluent
  public DesignDrivenRouterFactoryOptions setValidationFailureHandler(Handler<RoutingContext> validationFailureHandler) {
    this.validationFailureHandler = validationFailureHandler;
    return this;
  }

  public boolean isMountValidationFailureHandler() {
    return mountValidationFailureHandler;
  }

  /**
   * Enable or disable validation failure handler. If you enable it during router creation a failure handler
   * that manages ValidationException will be mounted. You can change the validation failure handler with with function {@link DesignDrivenRouterFactoryOptions#setValidationFailureHandler(Handler)}. If failure is different from ValidationException, next failure
   * handler will be called.
   *
   * @param mountGlobalValidationFailureHandler
   * @return this object
   */
  @Fluent
  public DesignDrivenRouterFactoryOptions setMountValidationFailureHandler(boolean mountGlobalValidationFailureHandler) {
    this.mountValidationFailureHandler = mountGlobalValidationFailureHandler;
    return this;
  }

  public Handler<RoutingContext> getNotImplementedFailureHandler() {
    return notImplementedFailureHandler;
  }

  /**
   * Set not implemented failure handler. It's called when you don't define an handler for a
   * specific operation. You can enable/disable this feature from
   * {@link DesignDrivenRouterFactoryOptions#setMountNotImplementedHandler(boolean)}
   *
   * @param notImplementedFailureHandler
   * @return this object
   */
  @Fluent
  public DesignDrivenRouterFactoryOptions setNotImplementedFailureHandler(Handler<RoutingContext> notImplementedFailureHandler) {
    this.notImplementedFailureHandler = notImplementedFailureHandler;
    return this;
  }

  public boolean isMountNotImplementedHandler() {
    return mountNotImplementedHandler;
  }

  /**
   * Automatic mount handlers that return HTTP 501 status code for operations where you didn't specify an handler.
   *
   * @param mountOperationsWithoutHandler
   * @return this object
   */
  @Fluent
  public DesignDrivenRouterFactoryOptions setMountNotImplementedHandler(boolean mountOperationsWithoutHandler) {
    this.mountNotImplementedHandler = mountOperationsWithoutHandler;
    return this;
  }

  public boolean isRequireSecurityHandlers() {
    return requireSecurityHandlers;
  }

  /**
   * If true, when you call {@link DesignDrivenRouterFactory#getRouter()} the factory will mount for every path
   * the required security handlers and, if a security handler is not defined, it throws an {@link RouterFactoryException}
   *
   * @param requireSecurityHandlers
   * @return this object
   */
  @Fluent
  public DesignDrivenRouterFactoryOptions setRequireSecurityHandlers(boolean requireSecurityHandlers) {
    this.requireSecurityHandlers = requireSecurityHandlers;
    return this;
  }
}
