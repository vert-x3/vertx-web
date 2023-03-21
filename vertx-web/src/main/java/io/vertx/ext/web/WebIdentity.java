/*
 * Copyright 2023 Red Hat, Inc.
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
package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

/**
 * Represents a WebIdentity. A web identity is coupled to the context user and is used to perform verifications
 * and actions on behalf of the user. Actions can be:
 *
 * <ul>
 *   <li>{@link  #refresh()} - Require a re-authentication to confirm the user is present</li>
 *   <li>{@link  #impersonate()} - Require a re-authentication to switch user identities</li>
 *   <li>{@link  #undo()} - De-escalate a previous impersonate call</li>
 * </ul>
 */
@VertxGen
public interface WebIdentity {

  /**
   * When performing a web identity operation, hint if possible to the identity provider to use the given login.
   * @param loginHint the desired login name, for example: {@code admin}.
   * @return fluent self
   */
  @Fluent
  WebIdentity loginHint(String loginHint);

  /**
   * Forces the current user to re-authenticate. The user will be redirected to the same origin where this call was
   * made. It is important to notice that the redirect will only allow sources originating from a HTTP GET request.
   *
   * @return future result of the operation.
   */
  Future<Void> refresh();

  /**
   * Forces the current user to re-authenticate. The user will be redirected to the given uri. It is important to
   * notice that the redirect will only allow targets using an HTTP GET request.
   *
   * @param redirectUri the uri to redirect the user to after the re-authentication.
   *
   * @return future result of the operation.
   */
  Future<Void> refresh(String redirectUri);

  /**
   * Impersonates a second identity. The user will be redirected to the same origin where this call was
   * made. It is important to notice that the redirect will only allow sources originating from a HTTP GET request.
   *
   * @return future result of the operation.
   */
  Future<Void> impersonate();

  /**
   * Impersonates a second identity. The user will be redirected to the given uri. It is important to
   * notice that the redirect will only allow targets using an HTTP GET request.
   *
   * @param redirectUri the uri to redirect the user to after the authentication.
   *
   * @return future result of the operation.
   */
  Future<Void> impersonate(String redirectUri);

  /**
   *  Undo a previous call to a impersonation. The user will be redirected to the same origin where this call was
   * made. It is important to notice that the redirect will only allow sources originating from a HTTP GET request.
   *
   * @return future result of the operation.
   */
  Future<Void> undo();

  /**
   * Undo a previous call to an impersonation. The user will be redirected to the given uri. It is important to
   * notice that the redirect will only allow targets using an HTTP GET request.
   *
   * @param redirectUri the uri to redirect the user to after the re-authentication.
   *
   * @return future result of the operation.
   */
  Future<Void> undo(String redirectUri);
}
