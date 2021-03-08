/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.impl.RouteImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author Paulo Lopes
 */
public class RouterExtendedParamTest extends WebTestBase {

  private static Field getAccessibleField(Class<?> clazz, String name) throws NoSuchFieldException, IllegalAccessException {
    Field field = clazz.getDeclaredField(name);
    field.setAccessible(true);

    //'modifiers' - it is a field of a class called 'Field'. Make it accessible and remove
    //'final' modifier for our 'CONSTANT' field
    Field modifiersField = Field.class.getDeclaredField( "modifiers" );
    modifiersField.setAccessible( true );
    modifiersField.setInt( field, field.getModifiers() & ~Modifier.FINAL );

    return field;
  }

  @Override
  public void setUp() throws Exception {
    // nasty setup patch the patterns
    Field reTokenSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_SEARCH");
    Field reTokenNameSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_NAME_SEARCH");
    // patch to use the extended version
    reTokenSearch.set(null, Pattern.compile(":([A-Za-z_$][A-Za-z0-9_$-]*)"));
    reTokenNameSearch.set(null, Pattern.compile("\\(\\?<([A-Za-z_$][A-Za-z0-9_$-]*)>"));
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    // nasty setup patch the patterns
    Field reTokenSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_SEARCH");
    Field reTokenNameSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_NAME_SEARCH");
    // patch to use the extended version
    reTokenSearch.set(null, Pattern.compile(":([A-Za-z0-9_]+)"));
    reTokenNameSearch.set(null, Pattern.compile("\\(\\?<([A-Za-z0-9_]+)>"));
  }

  @Test
  public void testRouteDashVariable() throws Exception {
    router.route("/foo/:my-id").handler(rc -> {
      assertEquals("123", rc.pathParam("my-id"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/foo/123", 200, "OK");
  }

  @Test
  public void testRouteDashVariableNOK() throws Exception {
    router.route("/flights/:from-:to").handler(rc -> {
      // from isn't set as the alphabet now includes -
      assertNull(rc.pathParam("from"));
      assertNotNull(rc.pathParam("from-"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/flights/LAX-SFO", 200, "OK");
  }

}
