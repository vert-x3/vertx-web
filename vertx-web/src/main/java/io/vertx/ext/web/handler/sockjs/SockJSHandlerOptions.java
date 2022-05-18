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

/**
 * @deprecated Use {@link SockJSOptions}.
 *
 * Options for configuring a SockJS handler
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@DataObject
@Deprecated
public class SockJSHandlerOptions extends SockJSOptions {

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

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public SockJSHandlerOptions(SockJSHandlerOptions other) {
    super(other);
  }

  /**
   * Default constructor.
   */
  public SockJSHandlerOptions() {
    super();
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public SockJSHandlerOptions(JsonObject json) {
    super(json);
  }
}
