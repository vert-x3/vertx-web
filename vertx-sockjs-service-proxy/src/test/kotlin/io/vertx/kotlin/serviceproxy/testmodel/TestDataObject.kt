package io.vertx.kotlin.serviceproxy.testmodel

import io.vertx.serviceproxy.testmodel.TestDataObject

/**
 * A function providing a DSL for building [io.vertx.serviceproxy.testmodel.TestDataObject] objects.
 *
 *
 * @param bool 
 * @param number 
 * @param string 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.serviceproxy.testmodel.TestDataObject original] using Vert.x codegen.
 */
fun TestDataObject(
  bool: Boolean? = null,
  number: Int? = null,
  string: String? = null): TestDataObject = io.vertx.serviceproxy.testmodel.TestDataObject().apply {

  if (bool != null) {
    this.setBool(bool)
  }
  if (number != null) {
    this.setNumber(number)
  }
  if (string != null) {
    this.setString(string)
  }
}

