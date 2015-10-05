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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a {@link SessionHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen(concrete = false)
public interface AuthHandler extends Handler<RoutingContext> {

  /**
   * Add a required authority for this auth handler
   *
   * @param authority  the authority
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addAuthority(String authority);

  /**
   * Add a set of required authorities for this auth handler
   *
   * @param authorities  the set of authorities
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addAuthorities(Set<String> authorities);
}
