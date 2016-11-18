package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.BridgeOptions

fun BridgeOptions(
        maxAddressLength: Int? = null,
    maxHandlersPerSocket: Int? = null,
    pingTimeout: Long? = null,
    replyTimeout: Long? = null): BridgeOptions = io.vertx.ext.web.handler.sockjs.BridgeOptions().apply {

    if (maxAddressLength != null) {
        this.maxAddressLength = maxAddressLength
    }

    if (maxHandlersPerSocket != null) {
        this.maxHandlersPerSocket = maxHandlersPerSocket
    }

    if (pingTimeout != null) {
        this.pingTimeout = pingTimeout
    }

    if (replyTimeout != null) {
        this.replyTimeout = replyTimeout
    }

}

