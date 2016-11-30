package io.vertx.kotlin.serviceproxy.testmodel

import io.vertx.serviceproxy.testmodel.TestDataObject

fun TestDataObject(
    bool: Boolean? = null,
  number: Int? = null,
  string: String? = null): TestDataObject = io.vertx.serviceproxy.testmodel.TestDataObject().apply {

  if (bool != null) {
    this.isBool = bool
  }

  if (number != null) {
    this.number = number
  }

  if (string != null) {
    this.string = string
  }

}

