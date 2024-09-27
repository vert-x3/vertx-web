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
module io.vertx.web.validation {

  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;
  requires static io.vertx.docgen;

  requires io.vertx.jsonschema;
  requires io.netty.codec.http;
  requires io.vertx.core;
  requires io.vertx.web;

  exports io.vertx.ext.web.validation;
  exports io.vertx.ext.web.validation.builder;

  exports io.vertx.ext.web.validation.impl to io.vertx.web.validation.tests;
  exports io.vertx.ext.web.validation.impl.body to io.vertx.web.validation.tests;
  exports io.vertx.ext.web.validation.impl.parser to io.vertx.web.validation.tests;
  exports io.vertx.ext.web.validation.impl.parameter to io.vertx.web.validation.tests;
}
