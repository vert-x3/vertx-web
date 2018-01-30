package io.vertx.kotlin.ext.web

import io.vertx.ext.web.Http2PushMapping

fun Http2PushMapping(
  extensionTarget: String? = null,
  filePath: String? = null,
  noPush: Boolean? = null): Http2PushMapping = io.vertx.ext.web.Http2PushMapping().apply {

  if (extensionTarget != null) {
    this.setExtensionTarget(extensionTarget)
  }
  if (filePath != null) {
    this.setFilePath(filePath)
  }
  if (noPush != null) {
    this.setNoPush(noPush)
  }
}

