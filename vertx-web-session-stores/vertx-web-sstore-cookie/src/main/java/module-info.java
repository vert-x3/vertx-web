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
module io.vertx.web.sstore.cookie {

  requires static io.vertx.codegen.api;

  requires io.vertx.core;
  requires io.vertx.web;

  exports io.vertx.ext.web.sstore.cookie;

  provides io.vertx.ext.web.sstore.SessionStore with io.vertx.ext.web.sstore.cookie.impl.CookieSessionStoreImpl;

}
