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
    router.route().handler(LocaleHandler.create().addResolver(LocaleResolver.acceptLanguageHeaderResolver()));
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
    router.route().handler(LocaleHandler.create()
                              .addResolver(LocaleResolver.acceptLanguageHeaderResolver())
                              .addSupportedLocale("fr-ca")
                              .addSupportedLocale("fr-fr")
                              .addSupportedLocale("en-gb"));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> {},
      resp -> { 
        assertEquals("fr-CA", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_noHandledLocales1() throws Exception {
    router.route().handler(LocaleHandler.create().addResolver(LocaleResolver.acceptLanguageHeaderResolver()));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "fr-fr" ), 
      resp -> { 
        assertEquals("fr-FR", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_noHandledLocales2() throws Exception {
    router.route().handler(LocaleHandler.create().addResolver(LocaleResolver.acceptLanguageHeaderResolver()));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "en-gb, fr-fr" ), 
      resp -> { 
        assertEquals("en-GB", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_handledLocales1() throws Exception {
    router.route().handler(LocaleHandler.create()
                              .addResolver(LocaleResolver.acceptLanguageHeaderResolver())
                              .addSupportedLocale("fr-ca")
                              .addSupportedLocale("fr-fr")
                              .addSupportedLocale("en-gb"));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "fr-be, en-gb"), 
      resp -> { 
        assertEquals("en-GB", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_handledLocales2() throws Exception {
    router.route().handler(LocaleHandler.create()
                              .addResolver(LocaleResolver.acceptLanguageHeaderResolver())
                              .addSupportedLocale("fr-ca")
                              .addSupportedLocale("fr-fr")
                              .addSupportedLocale("en-gb"));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "de" ), 
      resp -> { 
        assertEquals("fr-CA", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_acceptLanguage_handledLocales3() throws Exception {
    router.route().handler(LocaleHandler.create()
                              .addResolver(LocaleResolver.acceptLanguageHeaderResolver())
                              .addSupportedLocale("fr_CA")
                              .addSupportedLocale("fr_FR")
                              .addSupportedLocale("en_GB"));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> req.putHeader("accept-language", "de" ), 
      resp -> { 
        assertEquals("fr-CA", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

  @Test
  public void test_fallback1() throws Exception {
    router.route().handler(LocaleHandler.create()
                              .addResolver(LocaleResolver.acceptLanguageHeaderResolver())
                              .addResolver(LocaleResolver.fallbackResolver("fr-fr"))
                              .addSupportedLocale("fr_CA")
                              .addSupportedLocale("fr_FR")
                              .addSupportedLocale("en_GB"));
    router.route().handler(rc -> { rc.response().end(); });
    testRequest(HttpMethod.GET, "/", 
      req -> {}, 
      resp -> { 
        assertEquals("fr-FR", resp.headers().get(HttpHeaders.CONTENT_LANGUAGE));
      }, 
      200, "OK", null
    );
  }

}
