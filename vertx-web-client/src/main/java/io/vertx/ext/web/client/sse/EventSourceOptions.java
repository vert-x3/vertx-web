/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.vertx.ext.web.client.sse;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 *
 */
@DataObject(generateConverter = true)
public class EventSourceOptions extends HttpClientOptions {

  public final static long DEFAULT_RECONNECT_TIME = 60_000;
  /**
   * When the object is created, it must be initialized to false.
   */
  public final static boolean DEFAULT_WITH_CREDENTIALS = false;

  private String url;
  /**
   * A reconnection time, in milliseconds.
   * <p>
   * This must initially be a user-agent-defined value, probably in the region of a few seconds.
   */
  private long reconnectInterval;
  private boolean withCredentials;

  EventSourceOptions() {
    super();
    reconnectInterval = DEFAULT_RECONNECT_TIME;
    withCredentials = DEFAULT_WITH_CREDENTIALS;
  }

  public EventSourceOptions(final HttpClientOptions clientOptions) {
    super(clientOptions);
    this.reconnectInterval = DEFAULT_RECONNECT_TIME;
  }

  /**
   * Creates a new instance from JSON.
   */
  public EventSourceOptions(JsonObject json) {
    super(json);
    EventSourceOptionsConverter.fromJson(json, this);
  }

  public String getUrl() {
    return this.url;
  }

  public EventSourceOptions setUrl(final String url) {
    this.url = url;
    return this;
  }

  public boolean isWithCredentials() {
    return withCredentials;
  }

  public EventSourceOptions setWithCredentials(final boolean withCredentials) {
    this.withCredentials = withCredentials;
    return this;
  }

  public long getReconnectInterval() {
    return reconnectInterval;
  }

  public EventSourceOptions setReconnectInterval(long reconnectInterval) {
    this.reconnectInterval = reconnectInterval;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = super.toJson();
    EventSourceOptionsConverter.toJson(this, json);
    return json;
  }

}
