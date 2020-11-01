package io.vertx.ext.web.templ.rythm;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rythm.impl.RythmTemplateEngineImpl;
import org.rythmengine.RythmEngine;

/**
 * A template engine that uses the rythm library.
 * The {@link #unwrap()} shall return an object of class {@link RythmEngine}
 * @author Konstantin Volivach kostya05983@mail.ru
 */
@VertxGen
public interface RythmTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "html";

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static RythmTemplateEngine create(Vertx vertx) {
    return create(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static RythmTemplateEngine create(Vertx vertx, String extension) {
    return new RythmTemplateEngineImpl(vertx, extension);
  }
}
