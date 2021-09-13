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
package io.vertx.ext.web;

import io.vertx.codegen.annotations.VertxGen;

/**
 * What kind of forward header parsing are we allowing.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
public enum AllowForwardHeaders {

  /**
   * No parsing shall be performed.
   */
  NONE,

  /**
   * Only process the standard {@code Forward} header as defined by <a href="https://tools.ietf.org/html/rfc7239#section-4>RFC 7239, section 4: Forwarded</a>.
   *
   * For more info see: <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded">https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded</a>
   */
  FORWARD,

  /**
   * Only process the non standard but widely used {@code X-Forward-*} headers.
   *
   * These headers are not official standards but widely used. Users are advised to avoid them for new applications.
   */
  X_FORWARD,

  /**
   * Will process both {@link #FORWARD} and {@link #X_FORWARD}. Be aware that mixing the 2 headers can open
   * security holes as specially crafted requests that are not validated as proxy level can allow bypassing
   * the proxy desired forward value.
   *
   * For example, a proxy will add the {@code X-Forward-*} headers to a request but not filter out if the original
   * request includes the {@code Forward} header.
   */
  ALL
}
