package io.vertx.ext.web.templ.impl;

import groovy.text.Template;
import groovy.text.TemplateEngine;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.GroovyTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public class GroovyTemplateEngineImpl extends CachingTemplateEngine<Template> implements GroovyTemplateEngine {

  private groovy.text.TemplateEngine engine;

  public GroovyTemplateEngineImpl(groovy.text.TemplateEngine engine) {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    this.engine = engine;
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    Template tpl = cache.get(templateFileName);
    if (tpl != null) {
      renderTemplate(context, tpl, handler);
    } else {
      Vertx vertx = context.vertx();
      String loc = adjustLocation(templateFileName);
      String rawTpl = Utils.readFileToString(vertx, loc);
      if (rawTpl == null) {
        throw new IllegalArgumentException("Cannot find resource " + loc);
      }
      compileTemplate(vertx, rawTpl, compileResult -> {
        if (compileResult.failed()) {
          context.fail(compileResult.cause());
        } else {
          Template template = compileResult.result();
          cache.put(templateFileName, template);
          renderTemplate(context, template, handler);
        }
      });
    }
  }

  private void compileTemplate(Vertx vertx, String rawTpl, Handler<AsyncResult<Template>> handler) {
    vertx.executeBlocking(future -> {
      try {
        Template tpl = engine.createTemplate(rawTpl);
        future.complete(tpl);
      } catch (Exception e) {
        future.fail(e);
      }
    }, res -> {
      if (res.succeeded()) {
        handler.handle(Future.succeededFuture((Template) res.result()));
      } else {
        handler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  private void renderTemplate(RoutingContext context, Template template, Handler<AsyncResult<Buffer>> handler) {
    context.vertx().executeBlocking(future -> {
      Map<String, Object> binding = new HashMap<String, Object>();
      binding.put("context", context);
      try {
        future.complete(template.make(binding).toString());
      } catch (Exception e) {
        future.fail(e);
      }
    }, res -> {
      if (res.succeeded()) {
        handler.handle(Future.succeededFuture(Buffer.buffer((String) res.result())));
      } else {
        handler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public GroovyTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public GroovyTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public TemplateEngine getGroovyEngine() {
    return engine;
  }
}
