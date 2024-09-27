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
open module io.vertx.web.tests {

  exports io.vertx.ext.web.tests.handler;
  exports io.vertx.ext.web.tests;

  requires io.netty.transport;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires static io.vertx.auth.abac;
  requires static io.vertx.auth.oauth2;
  requires static io.vertx.auth.jwt;
  requires static io.vertx.auth.webauthn;
  requires static io.vertx.auth.htdigest;
  requires static io.vertx.auth.properties;
  requires static io.vertx.auth.otp;
  requires static io.vertx.healthcheck;
  requires io.vertx.core.tests;
  requires static io.vertx.testing.unit;
  requires io.vertx.eventbusbridge;
  requires io.vertx.web;
  requires io.vertx.web.common;
  requires junit;
  requires static testcontainers;
  requires io.netty.common;
  requires io.netty.codec.http;
  requires static hamcrest.core;
  requires static org.mockito;

}
