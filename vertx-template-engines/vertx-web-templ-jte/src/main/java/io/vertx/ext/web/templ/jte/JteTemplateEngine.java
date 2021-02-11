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

package io.vertx.ext.web.templ.jte;

import gg.jte.ContentType;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.jte.impl.JteTemplateEngineImpl;
import io.vertx.ext.web.templ.jte.impl.VertxDirectoryCodeResolver;

import java.nio.file.Paths;

/**
 * A template engine for <a href="https://jte.gg">jte</a> templates.
 *
 * @author <a href="mailto:andy@mazebert.com">Andreas Hager</a>
 */
@VertxGen
public interface JteTemplateEngine extends TemplateEngine {

  /**
   * Creates a vert.x template engine for jte templates with sane defaults.
   * <p>
   * Hot reloading is active, when {@link WebEnvironment#development()} is true.
   *
   * @param vertx                 the vert.x instance
   * @param templateRootDirectory the directory where jte templates are located
   * @return the created vert.x template engine
   */
  static JteTemplateEngine create(Vertx vertx, String templateRootDirectory) {
    return new JteTemplateEngineImpl(gg.jte.TemplateEngine.create(new VertxDirectoryCodeResolver(vertx, templateRootDirectory), Paths.get("target", "jte-classes"), ContentType.Html));
  }
}
