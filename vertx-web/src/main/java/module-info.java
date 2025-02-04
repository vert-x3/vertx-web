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
module io.vertx.web {

  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;
  requires static io.vertx.docgen;

  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires io.vertx.web.common;
  requires io.vertx.eventbusbridge;

  requires io.netty.common;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires com.fasterxml.jackson.core;

  // Required by Vert.x Web even when no Vert.x Auth handler is used
  requires transitive io.vertx.auth.common;

  requires static io.vertx.auth.htdigest;
  requires static io.vertx.auth.jwt;
  requires static io.vertx.auth.otp;
  requires static io.vertx.auth.oauth2;
  requires static io.vertx.auth.webauthn4j;
  requires static io.vertx.healthcheck;

  exports io.vertx.ext.web;
  exports io.vertx.ext.web.handler;
  exports io.vertx.ext.web.handler.sockjs;
  exports io.vertx.ext.web.healthchecks;
  exports io.vertx.ext.web.sstore;
  exports io.vertx.ext.web.internal.handler;

  exports io.vertx.ext.web.impl to io.vertx.web.tests, io.vertx.web.validation, io.vertx.web.apiservice, io.vertx.web.graphql;
  exports io.vertx.ext.web.sstore.impl to io.vertx.web.tests, io.vertx.web.sstore.redis;
  exports io.vertx.ext.web.handler.impl to io.vertx.web.tests;
  exports io.vertx.ext.web.handler.sockjs.impl to io.vertx.web.tests;

  uses io.vertx.ext.web.sstore.SessionStore;

}
