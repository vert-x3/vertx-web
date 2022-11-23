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
import io.vertx.core.json.JsonObject;

import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;

import java.util.List;

/**
 * Options for configuring the event bus bridge.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@DataObject(generateConverter = true)
public class SockJSBridgeOptions extends BridgeOptions {

  /**
   * Default value for max address length = 200
   */
  public static final int DEFAULT_MAX_ADDRESS_LENGTH = 200;

  /**
   * Default value for max handlers per socket = 1000
   */
  public static final int DEFAULT_MAX_HANDLERS_PER_SOCKET = 1000;

  /**
   * Default value for ping timeout = 10000 ms
   */
  public static final long DEFAULT_PING_TIMEOUT = 10 * 1000;

  /**
   * Default value for reply timeout = 30000
   */
  public static final long DEFAULT_REPLY_TIMEOUT = 30 * 1000;

  /**
   * Default value for Socket timeout = 50000 ms
   */
  public static final long DEFAULT_EVENT_BUS_TIMEOUT = 50 * 1000;


  private long eventBusTimeout;
  private int maxAddressLength;
  private int maxHandlersPerSocket;
  private long pingTimeout;
  private long replyTimeout;

  /**
   * Copy constructor
   *
   * @param other  the options to copy
   */
  public SockJSBridgeOptions(SockJSBridgeOptions other) {
    super(other);
    this.maxAddressLength = other.maxAddressLength;
    this.maxHandlersPerSocket = other.maxHandlersPerSocket;
    this.pingTimeout = other.pingTimeout;
    this.replyTimeout = other.replyTimeout;
    this.eventBusTimeout = other.eventBusTimeout;
  }

  /**
   * Default constructor
   */
  public SockJSBridgeOptions() {
    super();
    this.maxAddressLength = DEFAULT_MAX_ADDRESS_LENGTH;
    this.maxHandlersPerSocket = DEFAULT_MAX_HANDLERS_PER_SOCKET;
    this.pingTimeout = DEFAULT_PING_TIMEOUT;
    this.replyTimeout = DEFAULT_REPLY_TIMEOUT;
    this.eventBusTimeout = DEFAULT_EVENT_BUS_TIMEOUT;
  }

  /**
   * Constructor from JSON
   *
   * @param json  the JSON
   */
  public SockJSBridgeOptions(JsonObject json) {
    // init defaults
    this();
    SockJSBridgeOptionsConverter.fromJson(json, this);
  }

  public int getMaxAddressLength() {
    return maxAddressLength;
  }

  public SockJSBridgeOptions setMaxAddressLength(int maxAddressLength) {
    if (maxAddressLength < 1) {
      throw new IllegalArgumentException("maxAddressLength must be > 0");
    }
    this.maxAddressLength = maxAddressLength;
    return this;
  }

  public int getMaxHandlersPerSocket() {
    return maxHandlersPerSocket;
  }

  public SockJSBridgeOptions setMaxHandlersPerSocket(int maxHandlersPerSocket) {
    if (maxHandlersPerSocket < 1) {
      throw new IllegalArgumentException("maxHandlersPerSocket must be > 0");
    }
    this.maxHandlersPerSocket = maxHandlersPerSocket;
    return this;
  }

  public long getPingTimeout() {
    return pingTimeout;
  }

  public SockJSBridgeOptions setPingTimeout(long pingTimeout) {
    if (pingTimeout < 1) {
      throw new IllegalArgumentException("pingTimeout must be > 0");
    }
    this.pingTimeout = pingTimeout;
    return this;
  }

  public long getReplyTimeout() {
    return replyTimeout;
  }

  public SockJSBridgeOptions setReplyTimeout(long replyTimeout) {
    if (replyTimeout < 1) {
      throw new IllegalArgumentException("replyTimeout must be > 0");
    }
    this.replyTimeout = replyTimeout;
    return this;
  }

  public SockJSBridgeOptions setEventBusTimeout(long eventBusTimeout) {
    if (eventBusTimeout < 1) {
      throw new IllegalArgumentException("Eventbus must be > 0");
    }
    this.eventBusTimeout = eventBusTimeout;
    return this;
  }

  public long getEventBusTimeout(){
    return this.eventBusTimeout;
  }


  @Override
  public SockJSBridgeOptions addInboundPermitted(PermittedOptions permitted) {
    super.addInboundPermitted(permitted);
    return this;
  }

  @Override
  public SockJSBridgeOptions setInboundPermitteds(List<PermittedOptions> inboundPermitted) {
    super.setInboundPermitteds(inboundPermitted);
    return this;
  }

  @Override
  public SockJSBridgeOptions addOutboundPermitted(PermittedOptions permitted) {
    super.addOutboundPermitted(permitted);
    return this;
  }

  @Override
  public SockJSBridgeOptions setOutboundPermitteds(List<PermittedOptions> outboundPermitted) {
    super.setOutboundPermitteds(outboundPermitted);
    return this;
  }


  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    SockJSBridgeOptionsConverter.toJson(this, json);
    return json;
  }
}
