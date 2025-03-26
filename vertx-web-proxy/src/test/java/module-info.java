/*
 * Copyright 2025 Red Hat, Inc.
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
open module io.vertx.web.proxy.tests {
  requires io.vertx.core;
  requires io.vertx.httpproxy;
  requires io.vertx.web;
  requires io.vertx.web.proxy;
  requires io.vertx.web.tests;
  requires junit;
  requires io.vertx.auth.common;
}
