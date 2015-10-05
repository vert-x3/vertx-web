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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;

/**
 * The possible out of the box formats.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
public enum LoggerFormat {

  /**
   * <i>remote-client</i> - - [<i>timestamp</i>] "<i>method</i> <i>uri</i> <i>version</i>" <i>status</i> <i>content-length</i> "<i>referrer</i>" "<i>user-agent</i>"
   */
  DEFAULT,

  /**
   * <i>remote-client</i> - <i>method</i> <i>uri</i> <i>version</i> <i>status</i> <i>content-length</i> <i>duration</i> ms
   */
  SHORT,

  /**
   * <i>method</i> <i>uri</i> <i>status</i> - <i>content-length</i> <i>duration</i>
   */
  TINY
}
