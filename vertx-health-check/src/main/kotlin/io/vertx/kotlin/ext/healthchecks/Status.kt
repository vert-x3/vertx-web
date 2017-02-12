package io.vertx.kotlin.ext.healthchecks

import io.vertx.ext.healthchecks.Status

/**
 * A function providing a DSL for building [io.vertx.ext.healthchecks.Status] objects.
 *
 * Represents the outcome of a health check procedure. Each procedure produces a [io.vertx.ext.healthchecks.Status] indicating either OK
 * or KO. Optionally, it can also provide additional data.
 *
 * @param data  Sets the metadata.
 * @param ok  Sets whether or not the current status is positive (UP) or negative (DOWN).
 * @param procedureInError  Sets whether or not the procedure attached to this status has failed (timeout, error...).
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.healthchecks.Status original] using Vert.x codegen.
 */
fun Status(
  data: io.vertx.core.json.JsonObject? = null,
  ok: Boolean? = null,
  procedureInError: Boolean? = null): Status = io.vertx.ext.healthchecks.Status().apply {

  if (data != null) {
    this.setData(data)
  }
  if (ok != null) {
    this.setOk(ok)
  }
  if (procedureInError != null) {
    this.setProcedureInError(procedureInError)
  }
}

