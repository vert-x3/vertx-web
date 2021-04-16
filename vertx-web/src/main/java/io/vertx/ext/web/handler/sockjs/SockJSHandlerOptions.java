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

  /**
   * The default delay before sending a {@code close} event to a silent client.
   */
  public static final long DEFAULT_SESSION_TIMEOUT = 5L * 1000;

  /**
   * Whether a {@code JSESSIONID} cookie should be inserted by default = true.
   */
  public static final boolean DEFAULT_INSERT_JSESSIONID = true;

  /**
   * The default interval between heartbeat packets.
   */
  public static final long DEFAULT_HEARTBEAT_INTERVAL = 25L * 1000;

  /**
   * The default maximum number of bytes an HTTP streaming request can send.
   */
  public static final int DEFAULT_MAX_BYTES_STREAMING = 128 * 1024;

  /**
   * The default SockJS library URL to load in iframe when a transport does not support cross-domain communication natively.
   */
  public static final String DEFAULT_LIBRARY_URL = "//cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js";

  /**
   * Whether a {@code writeHandler} should be registered by default = false.
   */
  public static final boolean DEFAULT_REGISTER_WRITE_HANDLER = false;

  /**
   * Whether the {@code writeHandler} should be registered as local by default = true.
   */
  public static final boolean DEFAULT_LOCAL_WRITE_HANDLER = true;

  private long sessionTimeout;
  private boolean insertJSESSIONID;
  private long heartbeatInterval;
  private int maxBytesStreaming;
  private String libraryURL;
  private final Set<String> disabledTransports = new HashSet<>();
  private boolean registerWriteHandler;
  private boolean localWriteHandler;

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public SockJSHandlerOptions(SockJSHandlerOptions other) {
    sessionTimeout = other.sessionTimeout;
    insertJSESSIONID = other.insertJSESSIONID;
    heartbeatInterval = other.heartbeatInterval;
    maxBytesStreaming = other.maxBytesStreaming;
    libraryURL = other.libraryURL;
    disabledTransports.addAll(other.disabledTransports);
    registerWriteHandler = other.registerWriteHandler;
    localWriteHandler = other.localWriteHandler;
  }

  /**
   * Default constructor.
   */
  public SockJSHandlerOptions() {
    sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    insertJSESSIONID = DEFAULT_INSERT_JSESSIONID;
    heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    maxBytesStreaming = DEFAULT_MAX_BYTES_STREAMING;
    libraryURL = DEFAULT_LIBRARY_URL;
    registerWriteHandler = DEFAULT_REGISTER_WRITE_HANDLER;
    localWriteHandler = DEFAULT_LOCAL_WRITE_HANDLER;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public SockJSHandlerOptions(JsonObject json) {
    sessionTimeout = json.getLong("sessionTimeout", DEFAULT_SESSION_TIMEOUT);
    insertJSESSIONID = json.getBoolean("insertJSESSIONID", DEFAULT_INSERT_JSESSIONID);
    heartbeatInterval = json.getLong("heartbeatInterval", DEFAULT_HEARTBEAT_INTERVAL);
    maxBytesStreaming = json.getInteger("maxBytesStreaming", DEFAULT_MAX_BYTES_STREAMING);
    libraryURL = json.getString("libraryURL", DEFAULT_LIBRARY_URL);
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
    registerWriteHandler = json.getBoolean("registerWriteHandler", DEFAULT_REGISTER_WRITE_HANDLER);
    localWriteHandler = json.getBoolean("localWriteHandler", DEFAULT_LOCAL_WRITE_HANDLER);
  }

  /**
   * @return the session timeout in milliseconds
   */
  public long getSessionTimeout() {
    return sessionTimeout;
  }

  /**
   * Set the delay before the server sends a {@code close} event when a client receiving connection has not been seen for a while.
   * <p>
   * Defaults to 5 seconds.
   *
   * @param sessionTimeout timeout in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandlerOptions setSessionTimeout(long sessionTimeout) {
    if (sessionTimeout < 1) {
      throw new IllegalArgumentException("sessionTimeout must be > 0");
    }
    this.sessionTimeout = sessionTimeout;
    return this;
  }

  /**
   * @return true if a {@code JSESSIONID} cookie should be inserted, false otherwise
   */
  public boolean isInsertJSESSIONID() {
    return insertJSESSIONID;
  }

  /**
   * Whether to insert a {@code JSESSIONID} cookie so load-balancers ensure requests for a specific SockJS session are always routed to the correct server.
   * <p>
   * Defaults to {@code true}.
   *
   * @param insertJSESSIONID true if a {@code JSESSIONID} cookie should be inserted, false otherwise
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandlerOptions setInsertJSESSIONID(boolean insertJSESSIONID) {
    this.insertJSESSIONID = insertJSESSIONID;
    return this;
  }

  /**
   * @return the hearbeat packet interval in milliseconds
   */
  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  /**
   * In order to keep proxies and load balancers from closing long running HTTP requests we need to pretend that the connection is active and send a heartbeat packet once in a while.
   * This setting controls how often this is done.
   * <p>
   * Defaults to 25 seconds.
   *
   * @param heartbeatInterval interval in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandlerOptions setHeartbeatInterval(long heartbeatInterval) {
    if (heartbeatInterval < 1) {
      throw new IllegalArgumentException("heartbeatInterval must be > 0");
    }
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  /**
   * @return maximum number of bytes an HTTP streaming request can send
   */
  public int getMaxBytesStreaming() {
    return maxBytesStreaming;
  }

  /**
   * Most streaming transports save responses on the client side and don't free memory used by delivered messages.
   * Such transports need to be garbage-collected once in a while.
   * <p>
   * This setting controls the maximum number of bytes that can be sent over a single HTTP streaming request before it will be closed.
   * After that the client needs to open new request.
   * Setting this value to one effectively disables streaming and will make streaming transports to behave like polling transports.
   * <p>
   * Defaults to 128K.
   *
   * @param maxBytesStreaming maximum number of bytes an HTTP streaming request can send
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandlerOptions setMaxBytesStreaming(int maxBytesStreaming) {
    if (maxBytesStreaming < 1) {
      throw new IllegalArgumentException("maxBytesStreaming must be > 0");
    }
    this.maxBytesStreaming = maxBytesStreaming;
    return this;
  }

  /**
   * @return the SockJS library URL to load in iframe when a transport does not support cross-domain communication natively
   */
  public String getLibraryURL() {
    return libraryURL;
  }

  /**
   * Transports which don't support cross-domain communication natively use an iframe trick.
   * A simple page is served from the SockJS server (using its foreign domain) and is placed in an invisible iframe.
   * <p>
   * Code run from this iframe doesn't need to worry about cross-domain issues, as it's being run from domain local to the SockJS server.
   * This iframe also does need to load SockJS javascript client library, and this option lets you specify its URL.
   *
   * @param libraryURL the SockJS library URL
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandlerOptions setLibraryURL(String libraryURL) {
    this.libraryURL = libraryURL;
    return this;
  }

  /**
   * Add a transport (by name) to the set of disabled transports.
   *
   * @param subProtocol the transport to disable
   * @return a reference to this, so the API can be used fluently
   * @see Transport
   */
  public SockJSHandlerOptions addDisabledTransport(String subProtocol) {
    disabledTransports.add(subProtocol);
    return this;
  }

  /**
   * @return the set of transports to disable
   */
  public Set<String> getDisabledTransports() {
    return disabledTransports;
  }

  /**
   * @return true if a {@code writeHandler} should be registered on the {@link io.vertx.core.eventbus.EventBus}, false otherwise
   * @see SockJSSocket#writeHandlerID()
   */
  public boolean isRegisterWriteHandler() {
    return registerWriteHandler;
  }

  /**
   * Whether a {@code writeHandler} should be registered on the {@link io.vertx.core.eventbus.EventBus}.
   * <p>
   * Defaults to {@code false}.
   *
   * @param registerWriteHandler true to register a {@code writeHandler}
   * @return a reference to this, so the API can be used fluently
   * @see SockJSSocket#writeHandlerID()
   */
  public SockJSHandlerOptions setRegisterWriteHandler(boolean registerWriteHandler) {
    this.registerWriteHandler = registerWriteHandler;
    return this;
  }

  /**
   * @return true if the {@code writeHandler} is local only, false otherwise
   * @see SockJSSocket#writeHandlerID()
   */
  public boolean isLocalWriteHandler() {
    return localWriteHandler;
  }

  /**
   * Whether the {@code writeHandler} should be local only or cluster-wide.
   * <p>
   * Defaults to {@code true}.
   *
   * @param localWriteHandler true to register locally, false otherwise
   * @return a reference to this, so the API can be used fluently
   * @see SockJSSocket#writeHandlerID()
   */
  public SockJSHandlerOptions setLocalWriteHandler(boolean localWriteHandler) {
    this.localWriteHandler = localWriteHandler;
    return this;
  }
}
