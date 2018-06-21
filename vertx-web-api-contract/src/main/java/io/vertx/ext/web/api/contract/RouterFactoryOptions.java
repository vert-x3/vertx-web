package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@DataObject(generateConverter = true, publicConverter = false)
public class RouterFactoryOptions {

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
   * By default, RouterFactory loads validation failure handler
   */
  public final static boolean DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER = true;

  /**
   * Default not implemented handler. It sends a response with status code 501,
   * status message "Not Implemented" and empty body
   */
  public final static Handler<RoutingContext> DEFAULT_NOT_IMPLEMENTED_HANDLER = (routingContext) -> routingContext.response().setStatusCode(501).setStatusMessage("Not Implemented").end();


  /**
   * By default, RouterFactory mounts Not Implemented handler
   */
  public final static boolean DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER = true;

  /**
   * By default, RouterFactory requires security handlers
   * to be defined while calling getRouter() or it will throw an Exception
   */
  public final static boolean DEFAULT_REQUIRE_SECURITY_HANDLERS = true;

  /**
   * By default, RouterFactory will mount ResponseContentTypeHandler when required
   */
  public final static boolean DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER = true;

  private Handler<RoutingContext> validationFailureHandler;
  private boolean mountValidationFailureHandler;
  private Handler<RoutingContext> notImplementedFailureHandler;
  private boolean mountNotImplementedHandler;
  private boolean requireSecurityHandlers;
  private boolean mountResponseContentTypeHandler;
  private BodyHandler bodyHandler;
  private List<Handler<RoutingContext>> globalHandlers;

  public RouterFactoryOptions() {
    init();
  }

  public RouterFactoryOptions(JsonObject json) {
    init();
    RouterFactoryOptionsConverter.fromJson(json, this);
  }

  public RouterFactoryOptions(RouterFactoryOptions other) {
    this.validationFailureHandler = other.getValidationFailureHandler();
    this.mountValidationFailureHandler = other.isMountValidationFailureHandler();
    this.notImplementedFailureHandler = other.getNotImplementedFailureHandler();
    this.mountNotImplementedHandler = other.isMountNotImplementedHandler();
    this.requireSecurityHandlers = other.isRequireSecurityHandlers();
    this.mountResponseContentTypeHandler = other.isMountResponseContentTypeHandler();
    this.bodyHandler = other.getBodyHandler();
    this.globalHandlers = other.getGlobalHandlers();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RouterFactoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.validationFailureHandler = DEFAULT_VALIDATION_HANDLER;
    this.mountValidationFailureHandler = DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER;
    this.notImplementedFailureHandler = DEFAULT_NOT_IMPLEMENTED_HANDLER;
    this.mountNotImplementedHandler = DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER;
    this.requireSecurityHandlers = DEFAULT_REQUIRE_SECURITY_HANDLERS;
    this.mountResponseContentTypeHandler = DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER;
    this.bodyHandler = BodyHandler.create();
    this.globalHandlers = new ArrayList<>();
  }

  public Handler<RoutingContext> getValidationFailureHandler() {
    return validationFailureHandler;
  }

  /**
   * Set default validation failure handler. You can enable/disable this feature from
   * {@link RouterFactoryOptions#setMountValidationFailureHandler(boolean)}
   *
   * @param validationFailureHandler
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setValidationFailureHandler(Handler<RoutingContext> validationFailureHandler) {
    this.validationFailureHandler = validationFailureHandler;
    return this;
  }

  public boolean isMountValidationFailureHandler() {
    return mountValidationFailureHandler;
  }

  /**
   * Enable or disable validation failure handler. If you enable it during router creation a failure handler
   * that manages ValidationException will be mounted. You can change the validation failure handler with with function {@link RouterFactoryOptions#setValidationFailureHandler(Handler)}. If failure is different from ValidationException, next failure
   * handler will be called.
   *
   * @param mountGlobalValidationFailureHandler
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setMountValidationFailureHandler(boolean mountGlobalValidationFailureHandler) {
    this.mountValidationFailureHandler = mountGlobalValidationFailureHandler;
    return this;
  }

  public Handler<RoutingContext> getNotImplementedFailureHandler() {
    return notImplementedFailureHandler;
  }

  /**
   * Set not implemented failure handler. It's called when you don't define an handler for a
   * specific operation. You can enable/disable this feature from
   * {@link RouterFactoryOptions#setMountNotImplementedHandler(boolean)}
   *
   * @param notImplementedFailureHandler
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setNotImplementedFailureHandler(Handler<RoutingContext> notImplementedFailureHandler) {
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
  public RouterFactoryOptions setMountNotImplementedHandler(boolean mountOperationsWithoutHandler) {
    this.mountNotImplementedHandler = mountOperationsWithoutHandler;
    return this;
  }

  public boolean isRequireSecurityHandlers() {
    return requireSecurityHandlers;
  }

  /**
   * If true, when you call {@link RouterFactory#getRouter()} the factory will mount for every path
   * the required security handlers and, if a security handler is not defined, it throws an {@link RouterFactoryException}
   *
   * @param requireSecurityHandlers
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions setRequireSecurityHandlers(boolean requireSecurityHandlers) {
    this.requireSecurityHandlers = requireSecurityHandlers;
    return this;
  }

  public boolean isMountResponseContentTypeHandler() {
    return mountResponseContentTypeHandler;
  }

  /**
   * If true, when required, the factory will mount a {@link io.vertx.ext.web.handler.ResponseContentTypeHandler}
   * @param mountResponseContentTypeHandler
   * @return
   */
  @Fluent
  public RouterFactoryOptions setMountResponseContentTypeHandler(boolean mountResponseContentTypeHandler) {
    this.mountResponseContentTypeHandler = mountResponseContentTypeHandler;
    return this;
  }

  public BodyHandler getBodyHandler() {
    return bodyHandler;
  }

  /**
   * Supply your own BodyHandler if you would like to control body limit, uploads directory and deletion of uploaded files
   * @param bodyHandler
   * @return self
   */
  @Fluent
  public RouterFactoryOptions setBodyHandler(BodyHandler bodyHandler) {
    this.bodyHandler = bodyHandler;
    return this;
  }

  public List<Handler<RoutingContext>> getGlobalHandlers() {
    return globalHandlers;
  }

  /**
   * Add global handler to be applied prior to {@link io.vertx.ext.web.Router} being generated. <br/>
   * Please note that you should not add a body handler inside that list. If you want to modify the body handler, please use {@link RouterFactoryOptions#setBodyHandler(BodyHandler)}
   *
   * @param globalHandler
   * @return this object
   */
  @Fluent
  public RouterFactoryOptions addGlobalHandler(Handler<RoutingContext> globalHandler) {
    this.globalHandlers.add(globalHandler);
    return this;
  }
}
