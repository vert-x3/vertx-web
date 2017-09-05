package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.PermittedOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.handler.sockjs.PermittedOptions] objects.
 *
 * Specify a match to allow for inbound and outbound traffic using the
 * [io.vertx.ext.web.handler.sockjs.BridgeOptions].
 *
 * @param address 
 * @param addressRegex 
 * @param match 
 * @param requiredAuthority 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.handler.sockjs.PermittedOptions original] using Vert.x codegen.
 */
fun PermittedOptions(
  address: String? = null,
  addressRegex: String? = null,
  match: io.vertx.core.json.JsonObject? = null,
  requiredAuthority: String? = null): PermittedOptions = io.vertx.ext.web.handler.sockjs.PermittedOptions().apply {

  if (address != null) {
    this.setAddress(address)
  }
  if (addressRegex != null) {
    this.setAddressRegex(addressRegex)
  }
  if (match != null) {
    this.setMatch(match)
  }
  if (requiredAuthority != null) {
    this.setRequiredAuthority(requiredAuthority)
  }
}

