package io.vertx.kotlin.ext.web.handler.sockjs

import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions

fun SockJSHandlerOptions(
    heartbeatInterval: Long? = null,
  insertJSESSIONID: Boolean? = null,
  libraryURL: String? = null,
  maxBytesStreaming: Int? = null,
  sessionTimeout: Long? = null): SockJSHandlerOptions = io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions().apply {

  if (heartbeatInterval != null) {
    this.heartbeatInterval = heartbeatInterval
  }

  if (insertJSESSIONID != null) {
    this.isInsertJSESSIONID = insertJSESSIONID
  }

  if (libraryURL != null) {
    this.libraryURL = libraryURL
  }

  if (maxBytesStreaming != null) {
    this.maxBytesStreaming = maxBytesStreaming
  }

  if (sessionTimeout != null) {
    this.sessionTimeout = sessionTimeout
  }

}

