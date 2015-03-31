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

package io.vertx.ext.apex.handler;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.ApexTestBase;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

/**
 *
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 * 
 */
public class LocaleHandlerTest extends ApexTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void test_noAcceptLanguage_noHandledLocales() throws Exception {
    router.route().handler(LocaleHandler.create());
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> {},
      resp -> { 
        assertEquals(null, resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_noAcceptLanguage_handledLocales() throws Exception {
    router.route().handler(LocaleHandler.create(Arrays.asList(Locale.FRENCH, Locale.ENGLISH)));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> {},
      resp -> { 
        assertEquals("fr", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_noHandledLocales1() throws Exception {
    router.route().handler(LocaleHandler.create());
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "fr" ), 
      resp -> { 
        assertEquals("fr", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_noHandledLocales2() throws Exception {
    router.route().handler(LocaleHandler.create());
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "en, fr" ), 
      resp -> { 
        assertEquals("en", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_handledLocales1() throws Exception {
    router.route().handler(LocaleHandler.create(Arrays.asList(Locale.FRENCH, Locale.ENGLISH)));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "en,fr" ), 
      resp -> { 
        assertEquals("en", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_handledLocales2() throws Exception {
    router.route().handler(LocaleHandler.create(Arrays.asList(Locale.FRENCH, Locale.ENGLISH)));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "de" ), 
      resp -> { 
        assertEquals("fr", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

}
