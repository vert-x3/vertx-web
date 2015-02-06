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

package io.vertx.ext.apex;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.sstore.SessionStore;

import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Session {

  String id();

  long lastAccessed();

  void setAccessed();

  void destroy();

  boolean isDestroyed();

  long timeout();

  SessionStore sessionStore();

  boolean isLoggedIn();

  void logout();

  void setPrincipal(String principal);

  String getPrincipal();

  @Fluent
  Session put(String key, Object obj);

  <T> T get(String key);

  <T> T remove(String key);

  @GenIgnore
  Map<String, Object> data();

}
