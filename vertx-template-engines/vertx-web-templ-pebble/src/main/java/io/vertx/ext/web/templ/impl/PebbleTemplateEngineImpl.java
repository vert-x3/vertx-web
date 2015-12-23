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

package io.vertx.ext.web.templ.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.PebbleTemplateEngine;

/**
 * @author Dan Kristensen</a>
 */
public class PebbleTemplateEngineImpl extends CachingTemplateEngine<PebbleTemplate> implements PebbleTemplateEngine {

	private final PebbleEngine pebbleEngine;
	private final FileLoader fileLoader;
	private final ClasspathLoader classpathLoader;

	public PebbleTemplateEngineImpl() {
		super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);

		final List<Loader<?>> loaders = new ArrayList<>();
		classpathLoader = new ClasspathLoader();
		classpathLoader.setSuffix(DEFAULT_TEMPLATE_EXTENSION);
		fileLoader = new FileLoader();
		fileLoader.setSuffix(DEFAULT_TEMPLATE_EXTENSION);
		loaders.add(classpathLoader);
		loaders.add(fileLoader);
		loaders.add(new StringLoader());

		pebbleEngine = new PebbleEngine.Builder().loader(new DelegatingLoader(loaders)).build();
	}

	@Override
	public PebbleTemplateEngine setExtension(String extension) {
		doSetExtension(extension);
		fileLoader.setSuffix(extension);
		classpathLoader.setSuffix(extension);
		return this;
	}

	@Override
	public PebbleTemplateEngine setMaxCacheSize(int maxCacheSize) {
		this.cache.setMaxSize(maxCacheSize);
		return this;
	}

	@Override
	public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
		try {
			PebbleTemplate template = cache.get(templateFileName);
			if (template == null) {
				// real compile
				final String loc = adjustLocation(templateFileName);
				final String templateText = Utils.readFileToString(context.vertx(), loc);
				if (templateText == null) {
					throw new IllegalArgumentException("Cannot find template " + loc);
				}
				template = pebbleEngine.getTemplate(templateText);
				cache.put(templateFileName, template);
			}
			final Map<String, Object> variables = new HashMap<>(1);
			variables.put("context", context);
			final StringWriter stringWriter = new StringWriter();
			template.evaluate(stringWriter, variables);
			handler.handle(Future.succeededFuture(Buffer.buffer(stringWriter.toString())));
		} catch (final Exception ex) {
			handler.handle(Future.failedFuture(ex));
		}
	}

}
