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

import io.vertx.ext.bridge.PermittedOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Options for configuring the event bus bridge.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@DataObject
public class BridgeOptions {

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

  private int maxAddressLength;
  private int maxHandlersPerSocket;
  private long pingTimeout;
  private long replyTimeout;

  private List<PermittedOptions> inboundPermitted = new ArrayList<>();
  private List<PermittedOptions> outboundPermitted = new ArrayList<>();

  /**
   * Copy constructor
   *
   * @param other  the options to copy
   */
  public BridgeOptions(BridgeOptions other) {
    this.maxAddressLength = other.maxAddressLength;
    this.maxHandlersPerSocket = other.maxHandlersPerSocket;
    this.pingTimeout = other.pingTimeout;
    this.replyTimeout = other.replyTimeout;
    this.inboundPermitted = new ArrayList<>(other.inboundPermitted);
    this.outboundPermitted = new ArrayList<>(other.outboundPermitted);
  }

  /**
   * Default constructor
   */
  public BridgeOptions() {
    this.maxAddressLength = DEFAULT_MAX_ADDRESS_LENGTH;
    this.maxHandlersPerSocket = DEFAULT_MAX_HANDLERS_PER_SOCKET;
    this.pingTimeout = DEFAULT_PING_TIMEOUT;
    this.replyTimeout = DEFAULT_REPLY_TIMEOUT;
  }

  /**
   * Constructor from JSON
   *
   * @param json  the JSON
   */
  public BridgeOptions(JsonObject json) {
    this.maxAddressLength = json.getInteger("maxAddressLength", DEFAULT_MAX_ADDRESS_LENGTH);
    this.maxHandlersPerSocket = json.getInteger("maxHandlersPerSocket", DEFAULT_MAX_HANDLERS_PER_SOCKET);
    this.pingTimeout = json.getLong("pingTimeout", DEFAULT_PING_TIMEOUT);
    this.replyTimeout = json.getLong("replyTimeout", DEFAULT_REPLY_TIMEOUT);
    //TODO simplify common code
    JsonArray arr = json.getJsonArray("inboundPermitteds");
    if (arr != null) {
      for (Object obj: arr) {
        if (obj instanceof JsonObject) {
          JsonObject jobj = (JsonObject)obj;
          inboundPermitted.add(new PermittedOptions(jobj));
        } else {
          throw new IllegalArgumentException("Invalid type " + obj.getClass() + " in inboundPermitteds array");
        }
      }
    }
    arr = json.getJsonArray("outboundPermitteds");
    if (arr != null) {
      for (Object obj: arr) {
        if (obj instanceof JsonObject) {
          JsonObject jobj = (JsonObject)obj;
          outboundPermitted.add(new PermittedOptions(jobj));
        } else {
          throw new IllegalArgumentException("Invalid type " + obj.getClass() + " in outboundPermitteds array");
        }
      }
    }
  }

  public int getMaxAddressLength() {
    return maxAddressLength;
  }

  public BridgeOptions setMaxAddressLength(int maxAddressLength) {
    if (maxAddressLength < 1) {
      throw new IllegalArgumentException("maxAddressLength must be > 0");
    }
    this.maxAddressLength = maxAddressLength;
    return this;
  }

  public int getMaxHandlersPerSocket() {
    return maxHandlersPerSocket;
  }

  public BridgeOptions setMaxHandlersPerSocket(int maxHandlersPerSocket) {
    if (maxHandlersPerSocket < 1) {
      throw new IllegalArgumentException("maxHandlersPerSocket must be > 0");
    }
    this.maxHandlersPerSocket = maxHandlersPerSocket;
    return this;
  }

  public long getPingTimeout() {
    return pingTimeout;
  }

  public BridgeOptions setPingTimeout(long pingTimeout) {
    if (pingTimeout < 1) {
      throw new IllegalArgumentException("pingTimeout must be > 0");
    }
    this.pingTimeout = pingTimeout;
    return this;
  }

  public long getReplyTimeout() {
    return replyTimeout;
  }

  public BridgeOptions setReplyTimeout(long replyTimeout) {
    if (replyTimeout < 1) {
      throw new IllegalArgumentException("replyTimeout must be > 0");
    }
    this.replyTimeout = replyTimeout;
    return this;
  }

  public BridgeOptions addInboundPermitted(PermittedOptions permitted) {
    inboundPermitted.add(permitted);
    return this;
  }

  public List<PermittedOptions> getInboundPermitteds() {
    return inboundPermitted;
  }

  public void setInboundPermitted(List<PermittedOptions> inboundPermitted) {
    this.inboundPermitted = inboundPermitted;
  }

  public BridgeOptions addOutboundPermitted(PermittedOptions permitted) {
    outboundPermitted.add(permitted);
    return this;
  }

  public List<PermittedOptions> getOutboundPermitteds() {
    return outboundPermitted;
  }

  public void setOutboundPermitted(List<PermittedOptions> outboundPermitted) {
    this.outboundPermitted = outboundPermitted;
  }
}
