package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.PermittedOptions

fun PermittedOptions(
        address: String? = null,
    addressRegex: String? = null,
    match: io.vertx.core.json.JsonObject? = null,
    requiredAuthority: String? = null): PermittedOptions = io.vertx.ext.web.handler.sockjs.PermittedOptions().apply {

    if (address != null) {
        this.address = address
    }

    if (addressRegex != null) {
        this.addressRegex = addressRegex
    }

    if (match != null) {
        this.match = match
    }

    if (requiredAuthority != null) {
        this.requiredAuthority = requiredAuthority
    }

}

