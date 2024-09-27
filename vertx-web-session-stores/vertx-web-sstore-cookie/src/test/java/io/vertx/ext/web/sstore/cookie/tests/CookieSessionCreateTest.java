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

package io.vertx.ext.web.sstore.cookie.tests;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@RunWith(VertxUnitRunner.class)
public class CookieSessionCreateTest {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Test
  public void testCreateStoreAll() throws Exception {
    SessionStore store = SessionStore.create(rule.vertx(), new JsonObject().put("secret", "KeyboardCat!").put("salt", "salt").put("iv", "iv"));
    assertNotNull(store);
    assertTrue(store instanceof CookieSessionStore);
  }

  @Test
  public void testCreateStoreNoIV() throws Exception {
    SessionStore store = SessionStore.create(rule.vertx(), new JsonObject().put("secret", "KeyboardCat!").put("salt", "salt"));
    assertNotNull(store);
    assertTrue(store instanceof CookieSessionStore);
  }

  @Test
  public void testCreateStoreNoSalt() throws Exception {
    SessionStore store = SessionStore.create(rule.vertx(), new JsonObject().put("secret", "KeyboardCat!"));
    assertNotNull(store);
    // salt is missing, default to LocalStore as we don't want to leak session data if config isn't complete
    assertFalse(store instanceof CookieSessionStore);
  }
}
