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

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.VertxGen;

/**
 * The available SockJS transports
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public enum Transport {

  /**
   * <a href="http://www.rfc-editor.org/rfc/rfc6455.txt">rfc 6455</a>
   */
  WEBSOCKET,

  /**
   * <a href="http://dev.w3.org/html5/eventsource/">Event source</a>
   */
  EVENT_SOURCE,

  /**
   * <a href="http://cometdaily.com/2007/11/18/ie-activexhtmlfile-transport-part-ii/">HtmlFile</a>.
   */
  HTML_FILE,

  /**
   * Slow and old fashioned <a hred="https://developer.mozilla.org/en/DOM/window.postMessage">JSONP polling</a>.
   * This transport will show "busy indicator" (aka: "spinning wheel") when sending data.
   */
  JSON_P,

  /**
   * Long-polling using <a hred="https://secure.wikimedia.org/wikipedia/en/wiki/XMLHttpRequest#Cross-domain_requests">cross domain XHR</a>
   */
  XHR
}
