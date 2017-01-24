package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.PermittedOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.handler.sockjs.PermittedOptions] objects.
 *
 * Specify a match to allow for inbound and outbound traffic using the
 * [io.vertx.ext.web.handler.sockjs.BridgeOptions].
 *
 * @param address  The exact address the message is being sent to. If you want to allow messages based on an exact address you use this field.
 * @param addressRegex  A regular expression that will be matched against the address. If you want to allow messages based on a regular expression you use this field. If the [io.vertx.ext.web.handler.sockjs.PermittedOptions] value is specified this will be ignored.
 * @param match  This allows you to allow messages based on their structure. Any fields in the match must exist in the message with the same values for them to be allowed. This currently only works with JSON messages.
 * @param requiredAuthority  Declare a specific authority that user must have in order to allow messages
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

