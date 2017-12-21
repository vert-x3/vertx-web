package io.vertx.kotlin.ext.web.api.contract

import io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions] objects.
 *
 *
 * @param mountNotImplementedHandler  Automatic mount handlers that return HTTP 501 status code for operations where you didn't specify an handler.
 * @param mountValidationFailureHandler  Enable or disable validation failure handler. If you enable it during router creation a failure handler that manages ValidationException will be mounted. You can change the validation failure handler with with function [io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions]. If failure is different from ValidationException, next failure handler will be called.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions original] using Vert.x codegen.
 */
fun DesignDrivenRouterFactoryOptions(
  mountNotImplementedHandler: Boolean? = null,
  mountValidationFailureHandler: Boolean? = null): DesignDrivenRouterFactoryOptions = io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions().apply {

  if (mountNotImplementedHandler != null) {
    this.setMountNotImplementedHandler(mountNotImplementedHandler)
  }
  if (mountValidationFailureHandler != null) {
    this.setMountValidationFailureHandler(mountValidationFailureHandler)
  }
}

