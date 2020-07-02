package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author Francesco Guardiani @slinkydeveloper
 * @deprecated You should use the new module vertx-web-openapi
 */
@DataObject(generateConverter = true, publicConverter = false)
@Deprecated
public class RouterFactoryOptions {

  /**
   * By default, RouterFactory doesn't mount validation failure handler
   * @deprecated Router Factory won't manage the validation errors anymore. You must use {@link io.vertx.ext.web.Router#errorHandler(int, Handler)} with 400 error
   */
  @Deprecated
  public final static boolean DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER = false;

  /**
   * By default, RouterFactory mounts Not Implemented/Method Not Allowed handler
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

  /**
   * By default, RouterFactory will not expose operation configuration in the the routing context
   */
  public final static String DEFAULT_OPERATION_MODEL_KEY = null;

  private boolean mountValidationFailureHandler;
  private boolean mountNotImplementedHandler;
  private boolean requireSecurityHandlers;
  private boolean mountResponseContentTypeHandler;
  private String operationModelKey;

  public RouterFactoryOptions() {
    init();
  }

  public RouterFactoryOptions(JsonObject json) {
    init();
    RouterFactoryOptionsConverter.fromJson(json, this);
  }

  public RouterFactoryOptions(RouterFactoryOptions other) {
    this.mountValidationFailureHandler = other.isMountValidationFailureHandler();
    this.mountNotImplementedHandler = other.isMountNotImplementedHandler();
    this.requireSecurityHandlers = other.isRequireSecurityHandlers();
    this.mountResponseContentTypeHandler = other.isMountResponseContentTypeHandler();
    this.operationModelKey = other.getOperationModelKey();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RouterFactoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.mountValidationFailureHandler = DEFAULT_MOUNT_VALIDATION_FAILURE_HANDLER;
    this.mountNotImplementedHandler = DEFAULT_MOUNT_NOT_IMPLEMENTED_HANDLER;
    this.requireSecurityHandlers = DEFAULT_REQUIRE_SECURITY_HANDLERS;
    this.mountResponseContentTypeHandler = DEFAULT_MOUNT_RESPONSE_CONTENT_TYPE_HANDLER;
    this.operationModelKey = DEFAULT_OPERATION_MODEL_KEY;
  }

  /**
   * @deprecated Router Factory won't manage the validation errors anymore. You must use {@link io.vertx.ext.web.Router#errorHandler(int, Handler)} with 400 error
   * @return
   */
  @Deprecated
  public boolean isMountValidationFailureHandler() {
    return mountValidationFailureHandler;
  }

  /**
   * Enable or disable validation failure handler. If you enable it during router creation a failure handler
   * that manages ValidationException will be mounted. You can change the validation failure handler with with function {@link RouterFactory#setValidationFailureHandler(Handler)}. If failure is different from ValidationException, next failure
   * handler will be called.
   *
   * @param mountGlobalValidationFailureHandler
   * @return this object
   * @deprecated Router Factory won't manage the validation errors anymore. You must use {@link io.vertx.ext.web.Router#errorHandler(int, Handler)} with 400 error
   */
  @Fluent
  @Deprecated
  public RouterFactoryOptions setMountValidationFailureHandler(boolean mountGlobalValidationFailureHandler) {
    this.mountValidationFailureHandler = mountGlobalValidationFailureHandler;
    return this;
  }

  public boolean isMountNotImplementedHandler() {
    return mountNotImplementedHandler;
  }

  /**
   * If true, Router Factory will automatically mount an handler that return HTTP 405/501 status code for each operation where you didn't specify an handler.
   * You can customize the response with {@link io.vertx.ext.web.Router#errorHandler(int, Handler)}
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

  public String getOperationModelKey() {
    return operationModelKey;
  }

  /**
   * When set, an additional handler will be created to expose the operation model in the routing
   * context under the given key. When the key is null, the handler is not added.
   * @param operationModelKey
   * @return
   */
  @Fluent
  public RouterFactoryOptions setOperationModelKey(String operationModelKey) {
    this.operationModelKey = operationModelKey;
    return this;
  }
}
