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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Locale;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.Thymeleaf3TemplateEngine;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://matty.io">Matty Southall</a>
 */
public class Thymeleaf3TemplateEngineImpl implements Thymeleaf3TemplateEngine {
    private final TemplateEngine templateEngine = new TemplateEngine();
    private ResourceTemplateResolver templateResolver;

    public Thymeleaf3TemplateEngineImpl() {
        ResourceTemplateResolver templateResolver = new ResourceTemplateResolver();
        templateResolver.setTemplateMode(Thymeleaf3TemplateEngine.DEFAULT_TEMPLATE_MODE);

        this.templateResolver = templateResolver;
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public Thymeleaf3TemplateEngine setMode(TemplateMode mode) {
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return this;
    }

    @Override
    public TemplateEngine getThymeleafTemplateEngine() {
        return this.templateEngine;
    }

    @Override
    public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
        Buffer buffer = Buffer.buffer();

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("context", context);
            data.putAll(context.data());

            synchronized (this) {
                templateResolver.setVertx(context.vertx());

                final List<Locale> acceptableLocales = context.acceptableLocales();

                io.vertx.ext.web.Locale locale;

                if (acceptableLocales.size() == 0) {
                    locale = io.vertx.ext.web.Locale.create();
                } else {
                    // this is the users preferred locale
                    locale = acceptableLocales.get(0);
                }

                templateEngine.process(templateFileName, new WebIContext(data, locale), new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) throws IOException {
                        buffer.appendString(new String(cbuf, off, len));
                    }

                    @Override
                    public void flush() throws IOException {
                    }

                    @Override
                    public void close() throws IOException {
                    }
                });
            }

            handler.handle(Future.succeededFuture(buffer));
        } catch (Exception ex) {
            handler.handle(Future.failedFuture(ex));
        }
    }

    private static class WebIContext implements IContext {
        private final Map<String, Object> data;
        private final java.util.Locale locale;

        private WebIContext(Map<String, Object> data, io.vertx.ext.web.Locale locale) {
            this.data = data;
            this.locale = new java.util.Locale(locale.language(), locale.country(), locale.variant());
        }

        @Override
        public java.util.Locale getLocale() {
            return locale;
        }

        @Override
        public boolean containsVariable(String name) {
            return data.containsKey(name);
        }

        @Override
        public Set<String> getVariableNames() {
            return data.keySet();
        }

        @Override
        public Object getVariable(String name) {
            return data.get(name);
        }
    }

    private static class ResourceTemplateResolver extends StringTemplateResolver {
        private Vertx vertx;

        public ResourceTemplateResolver() {
            super();
            setName("vertx-web/Thymeleaf3");
        }

        void setVertx(Vertx vertx) {
            this.vertx = vertx;
        }

        @Override
        protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
            String str = Utils.readFileToString(vertx, template);
            return new StringTemplateResource(str);
        }
    }
}
