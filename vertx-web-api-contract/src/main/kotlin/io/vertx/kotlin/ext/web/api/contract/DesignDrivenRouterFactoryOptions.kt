package io.vertx.kotlin.ext.web.api.contract

import io.vertx.ext.web.api.contract.RouterFactoryOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.api.contract.RouterFactoryOptions] objects.
 *
 *
 * @param mountNotImplementedHandler  Automatic mount handlers that return HTTP 501 status code for operations where you didn't specify an handler.
 * @param mountResponseContentTypeHandler  If true, when required, the factory will mount a [io.vertx.ext.web.handler.ResponseContentTypeHandler]
 * @param mountValidationFailureHandler  Enable or disable validation failure handler. If you enable it during router creation a failure handler that manages ValidationException will be mounted. You can change the validation failure handler with with function [io.vertx.ext.web.api.contract.RouterFactoryOptions]. If failure is different from ValidationException, next failure handler will be called.
 * @param requireSecurityHandlers  If true, when you call [io.vertx.ext.web.api.contract.RouterFactory] the factory will mount for every path the required security handlers and, if a security handler is not defined, it throws an [io.vertx.ext.web.api.contract.RouterFactoryException]
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.api.contract.RouterFactoryOptions original] using Vert.x codegen.
 */
fun DesignDrivenRouterFactoryOptions(
  mountNotImplementedHandler: Boolean? = null,
  mountResponseContentTypeHandler: Boolean? = null,
  mountValidationFailureHandler: Boolean? = null,
  requireSecurityHandlers: Boolean? = null): RouterFactoryOptions = RouterFactoryOptions().apply {

  if (mountNotImplementedHandler != null) {
    this.setMountNotImplementedHandler(mountNotImplementedHandler)
  }
  if (mountResponseContentTypeHandler != null) {
    this.setMountResponseContentTypeHandler(mountResponseContentTypeHandler)
  }
  if (mountValidationFailureHandler != null) {
    this.setMountValidationFailureHandler(mountValidationFailureHandler)
  }
  if (requireSecurityHandlers != null) {
    this.setRequireSecurityHandlers(requireSecurityHandlers)
  }
}

