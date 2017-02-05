package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.handler.sockjs.BridgeOptions] objects.
 *
 * Options for configuring the event bus bridge.
 *
 * @param inboundPermitted 
 * @param inboundPermitteds 
 * @param maxAddressLength 
 * @param maxHandlersPerSocket 
 * @param outboundPermitted 
 * @param outboundPermitteds 
 * @param pingTimeout 
 * @param replyTimeout 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.handler.sockjs.BridgeOptions original] using Vert.x codegen.
 */
fun BridgeOptions(
  inboundPermitted: Iterable<io.vertx.ext.web.handler.sockjs.PermittedOptions>? = null,
  inboundPermitteds: Iterable<io.vertx.ext.web.handler.sockjs.PermittedOptions>? = null,
  maxAddressLength: Int? = null,
  maxHandlersPerSocket: Int? = null,
  outboundPermitted: Iterable<io.vertx.ext.web.handler.sockjs.PermittedOptions>? = null,
  outboundPermitteds: Iterable<io.vertx.ext.web.handler.sockjs.PermittedOptions>? = null,
  pingTimeout: Long? = null,
  replyTimeout: Long? = null): BridgeOptions = io.vertx.ext.web.handler.sockjs.BridgeOptions().apply {

  if (inboundPermitted != null) {
    this.setInboundPermitted(inboundPermitted.toList())
  }
  if (inboundPermitteds != null) {
    for (item in inboundPermitteds) {
      this.addInboundPermitted(item)
    }
  }
  if (maxAddressLength != null) {
    this.setMaxAddressLength(maxAddressLength)
  }
  if (maxHandlersPerSocket != null) {
    this.setMaxHandlersPerSocket(maxHandlersPerSocket)
  }
  if (outboundPermitted != null) {
    this.setOutboundPermitted(outboundPermitted.toList())
  }
  if (outboundPermitteds != null) {
    for (item in outboundPermitteds) {
      this.addOutboundPermitted(item)
    }
  }
  if (pingTimeout != null) {
    this.setPingTimeout(pingTimeout)
  }
  if (replyTimeout != null) {
    this.setReplyTimeout(replyTimeout)
  }
}

