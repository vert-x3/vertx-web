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
module io.vertx.web.openapi.router {

  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;
  requires static io.vertx.docgen;

  requires static io.vertx.auth.common; // Examples
  requires static io.vertx.auth.oauth2; // Examples
  requires static io.vertx.auth.jwt;    // Examples

  requires io.vertx.openapi;
  requires io.vertx.web;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires io.netty.codec.http;

  exports io.vertx.ext.web.openapi.router;

  exports io.vertx.ext.web.openapi.router.impl to io.vertx.web.openapi.router.tests;
  exports io.vertx.ext.web.openapi.router.internal.handler to io.vertx.web.apiservice;
}
