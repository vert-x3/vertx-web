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
module io.vertx.web.common {

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

  requires io.vertx.core;

  exports io.vertx.ext.web.common;
  exports io.vertx.ext.web.common.template;
  exports io.vertx.ext.web.codec;
  exports io.vertx.ext.web.codec.spi;
  exports io.vertx.ext.web.multipart;

  exports io.vertx.ext.web.codec.impl to io.vertx.web.client;
}
