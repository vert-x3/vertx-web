/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Options for configuring a SockJS handler
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@DataObject
public class SockJSHandlerOptions {

  public static final long DEFAULT_SESSION_TIMEOUT = 5L * 1000;
  public static final boolean DEFAULT_INSERT_JSESSIONID = true;
  public static final long DEFAULT_HEARTBEAT_INTERVAL = 25L * 1000;
  public static final int DEFAULT_MAX_BYTES_STREAMING = 128 * 1024;
  public static final String DEFAULT_LIBRARY_URL = "//cdn.jsdelivr.net/sockjs/1.1.5/sockjs.min.js";

  private long sessionTimeout;
  private boolean insertJSESSIONID;
  private long heartbeatInterval;
  private int maxBytesStreaming;
  private String libraryURL;
  private Set<String> disabledTransports = new HashSet<>();

  public SockJSHandlerOptions(SockJSHandlerOptions other) {
    throw new UnsupportedOperationException("todo");
  }

  public SockJSHandlerOptions() {
    this.sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    this.insertJSESSIONID = DEFAULT_INSERT_JSESSIONID;
    this.heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    this.maxBytesStreaming = DEFAULT_MAX_BYTES_STREAMING;
    this.libraryURL = DEFAULT_LIBRARY_URL;
  }

  public SockJSHandlerOptions(JsonObject json) {
    this.sessionTimeout = json.getLong("sessionTimeout", DEFAULT_SESSION_TIMEOUT);
    this.insertJSESSIONID = json.getBoolean("insertJSESSIONID", DEFAULT_INSERT_JSESSIONID);
    this.heartbeatInterval = json.getLong("heartbeatInterval", DEFAULT_HEARTBEAT_INTERVAL);
    this.maxBytesStreaming = json.getInteger("maxBytesStreaming", DEFAULT_MAX_BYTES_STREAMING);
    this.libraryURL = json.getString("libraryURL", DEFAULT_LIBRARY_URL);
    JsonArray arr = json.getJsonArray("disabledTransports");
    if (arr != null) {
      for (Object str : arr) {
        if (str instanceof String) {
          String sstr = (String) str;
          disabledTransports.add(sstr);
        } else {
          throw new IllegalArgumentException("Invalid type " + str.getClass() + " in disabledTransports array");
        }
      }
    }
  }

  public long getSessionTimeout() {
    return sessionTimeout;
  }

  public SockJSHandlerOptions setSessionTimeout(long sessionTimeout) {
    if (sessionTimeout < 1) {
      throw new IllegalArgumentException("sessionTimeout must be > 0");
    }
    this.sessionTimeout = sessionTimeout;
    return this;
  }

  public boolean isInsertJSESSIONID() {
    return insertJSESSIONID;
  }

  public SockJSHandlerOptions setInsertJSESSIONID(boolean insertJSESSIONID) {
    this.insertJSESSIONID = insertJSESSIONID;
    return this;
  }

  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public SockJSHandlerOptions setHeartbeatInterval(long heartbeatInterval) {
    if (heartbeatInterval < 1) {
      throw new IllegalArgumentException("heartbeatInterval must be > 0");
    }
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  public int getMaxBytesStreaming() {
    return maxBytesStreaming;
  }

  public SockJSHandlerOptions setMaxBytesStreaming(int maxBytesStreaming) {
    if (maxBytesStreaming < 1) {
      throw new IllegalArgumentException("maxBytesStreaming must be > 0");
    }
    this.maxBytesStreaming = maxBytesStreaming;
    return this;
  }

  public String getLibraryURL() {
    return libraryURL;
  }

  public SockJSHandlerOptions setLibraryURL(String libraryURL) {
    this.libraryURL = libraryURL;
    return this;
  }

  public SockJSHandlerOptions addDisabledTransport(String subProtocol) {
    disabledTransports.add(subProtocol);
    return this;
  }

  public Set<String> getDisabledTransports() {
    return disabledTransports;
  }

}
