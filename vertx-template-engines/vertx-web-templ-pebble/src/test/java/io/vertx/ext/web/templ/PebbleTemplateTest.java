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

package io.vertx.ext.web.templ;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;

/**
 * @author Dan Kristensen
 */
public class PebbleTemplateTest extends WebTestBase {

	@Test
	public void testTemplateHandlerOnClasspath() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "somedir", "test-pebble-template2.peb",
		        "Hello badger and foxRequest path is /test-pebble-template2.peb");
	}

	@Test
	public void testTemplateHandlerOnFileSystem() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-template3.peb",
		        "Hello badger and foxRequest path is /test-pebble-template3.peb");
	}

	@Test
	public void testTemplateHandlerOnClasspathDisableCaching() throws Exception {
		System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
		testTemplateHandlerOnClasspath();
	}

	@Test
	public void testTemplateHandlerNoExtension() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "somedir", "test-pebble-template2", "Hello badger and foxRequest path is /test-pebble-template2");
	}

	@Test
	public void testTemplateHandlerChangeExtension() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx).setExtension("beb");
		testTemplateHandler(engine, "somedir", "test-pebble-template2", "Cheerio badger and foxRequest path is /test-pebble-template2");
	}

	private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName, String expected) throws Exception {
		router.route().handler(context -> {
			context.put("foo", "badger");
			context.put("bar", "fox");
			context.next();
		});
		router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
		testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
	}

	@Test
	public void testNoSuchTemplate() throws Exception {
		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.peb", "text/plain"));
		testRequest(HttpMethod.GET, "/foo.peb", 500, "Internal Server Error");
	}

	@Test
	public void testTemplateComplex() throws Exception {

		String expected = "Hello.Hi fox.\nHi badger!Footer - badger";

		final TemplateEngine engine = PebbleTemplateEngine.create(vertx);
		testTemplateHandler(engine, "src/test/filesystemtemplates", "test-pebble-complex.peb", expected);
	}
}
