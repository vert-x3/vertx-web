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

package io.vertx.ext.web.templ.groovy.impl;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Utils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.groovy.GroovyTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class GroovyTemplateEngineImpl extends CachingTemplateEngine<Template> implements GroovyTemplateEngine {

  private final Vertx vertx;
  private final GStringTemplateEngine engine = new GStringTemplateEngine();

  public GroovyTemplateEngineImpl (Vertx vertx, String extension) {
    super(vertx, extension);
    this.vertx = vertx;
  }

  private static final ThreadLocal<Map<String,Object>> BINDING = new ThreadLocal<>();
  private final Function<String,String> include = this::include;

  @Override
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    try {
      String src = adjustLocation(templateFile).replace('\\','/');// windows works with linux / path separator
      TemplateHolder<Template> template = getTemplate(src);

      if (template == null) {
        int idx = src.lastIndexOf('/');
        String baseDir = "";
        if (idx != -1) {
          baseDir = src.substring(0, idx);
        }

        if (!vertx.fileSystem().existsBlocking(src)) {
          return Future.failedFuture("Cannot find groovy template " + src);
        }

        template = new TemplateHolder<>(
          engine.createTemplate(readFile(src)),
          baseDir);

        putTemplate(src, template);
      }//else use from cache

      final Template gt = template.template();
      final String baseDir = template.baseDir();
      Map<String,Object> binding = JsonObject.of(
        "vertx", vertx,
        "baseDir", baseDir,
        "include", include
      ).getMap();
      binding.putAll(context);
      BINDING.set(binding);

      return Future.succeededFuture(
        Buffer.buffer(
          gt.make(binding).toString())
        );
    } catch (Exception ex) {
      return Future.failedFuture(ex);
    } finally {
      BINDING.remove();
    }
  }

  String include (String templateFile){
    try {
      String src = templateFile.trim().replace('\\', '/');

      Map<String,Object> binding = BINDING.get();
      String baseDir = "";
      if (binding != null && binding.get("baseDir") != null && binding.get("baseDir").toString().length() > 0){
        baseDir = binding.get("baseDir").toString();
        src = baseDir + '/' + src;
      }

      TemplateHolder<Template> template = getTemplate(src);

      if (template == null){
        if (!vertx.fileSystem().existsBlocking(src)){
          throw new IllegalStateException("Cannot find groovy sub-template " + src);
        }

        template = new TemplateHolder<>(
          engine.createTemplate(readFile(src)),
          baseDir);

        putTemplate(src, template);
      }//else use from cache

      final Template gt = template.template();

      return trimRightEol(gt.make(binding).toString());
    } catch (Exception ex){
      Utils.throwAsUnchecked(ex);
      return null;
    }
  }

  private String readFile (String src){
    return vertx.fileSystem()
      .readFileBlocking(src)
      .toString(StandardCharsets.UTF_8);
  }

  public static String trimRightEol (String s){
    if (s.endsWith("\r\n") || s.endsWith("\n\r")){
      return s.substring(0, s.length() - 2);

    } else if (s.endsWith("\n") || s.endsWith("\r")){
      return s.substring(0, s.length() - 1);

    } else {
      return s;
    }
  }
}
