package io.vertx.kotlin.ext.web.api.contract.openapi3

import io.vertx.ext.web.api.contract.openapi3.DesignDrivenRouterFactoryOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.api.contract.openapi3.DesignDrivenRouterFactoryOptions] objects.
 *
 *
 * @param mountGlobalValidationFailureHandler 
 * @param mountOperationsWithoutHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.api.contract.openapi3.DesignDrivenRouterFactoryOptions original] using Vert.x codegen.
 */
fun DesignDrivenRouterFactoryOptions(
  mountGlobalValidationFailureHandler: Boolean? = null,
  mountOperationsWithoutHandler: Boolean? = null): DesignDrivenRouterFactoryOptions = io.vertx.ext.web.api.contract.openapi3.DesignDrivenRouterFactoryOptions().apply {

  if (mountGlobalValidationFailureHandler != null) {
    this.setMountGlobalValidationFailureHandler(mountGlobalValidationFailureHandler)
  }
  if (mountOperationsWithoutHandler != null) {
    this.setMountOperationsWithoutHandler(mountOperationsWithoutHandler)
  }
}

