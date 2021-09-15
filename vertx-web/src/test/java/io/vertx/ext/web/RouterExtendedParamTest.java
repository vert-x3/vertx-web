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

import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author Paulo Lopes
 */
public class RouterExtendedParamTest extends WebTestBase {

  private static int getVersion() {
    String version = System.getProperty("java.version");
    if(version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if(dot != -1) { version = version.substring(0, dot); }
    } return Integer.parseInt(version);
  }

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

  public void patch() throws Exception {
    // nasty setup patch the patterns
    Field reTokenSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_SEARCH");
    Field reTokenNameSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_NAME_SEARCH");
    // patch to use the extended version
    reTokenSearch.set(null, Pattern.compile(":([A-Za-z_$][A-Za-z0-9_$-]*)"));
    reTokenNameSearch.set(null, Pattern.compile("\\(\\?<([A-Za-z_$][A-Za-z0-9_$-]*)>"));
  }

  public void unpatch() throws Exception {
    // nasty setup patch the patterns
    Field reTokenSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_SEARCH");
    Field reTokenNameSearch = getAccessibleField(RouteImpl.class, "RE_TOKEN_NAME_SEARCH");
    // patch to use the extended version
    reTokenSearch.set(null, Pattern.compile(":([A-Za-z0-9_]+)"));
    reTokenNameSearch.set(null, Pattern.compile("\\(\\?<([A-Za-z0-9_]+)>"));
  }

  @Test
  public void testRouteDashVariable() throws Exception {
    assumeTrue("Java >= 17 doesn't allow changing final static fields", getVersion() < 17);
    try {
      patch();
      router.route("/foo/:my-id").handler(rc -> {
        assertEquals("123", rc.pathParam("my-id"));
        rc.response().end();
      });
      testRequest(HttpMethod.GET, "/foo/123", 200, "OK");
    } finally {
      unpatch();
    }
  }

  @Test
  public void testRouteDashVariableNOK() throws Exception {
    assumeTrue("Java >= 17 doesn't allow changing final static fields", getVersion() < 17);
    try {
      patch();
      router.route("/flights/:from-:to").handler(rc -> {
        // from isn't set as the alphabet now includes -
        assertNull(rc.pathParam("from"));
        assertNotNull(rc.pathParam("from-"));
        rc.response().end();
      });
      testRequest(HttpMethod.GET, "/flights/LAX-SFO", 200, "OK");
    } finally {
      unpatch();
    }
  }

}
