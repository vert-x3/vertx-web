/*
 * Copyright 2024 Red Hat, Inc.
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
module io.vertx.web.client {

  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;
  requires static io.vertx.docgen;

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires static io.vertx.auth.oauth2;
  requires io.vertx.uritemplate;
  requires io.vertx.web.common;
  requires io.netty.common;

  exports io.vertx.ext.web.client;
  exports io.vertx.ext.web.client.spi;
  exports io.vertx.ext.web.client.impl to io.vertx.web.client.tests;
  exports io.vertx.ext.web.client.impl.cache to io.vertx.web.client.tests;

}
