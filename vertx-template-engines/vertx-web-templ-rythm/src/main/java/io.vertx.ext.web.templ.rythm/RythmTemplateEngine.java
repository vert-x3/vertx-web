package io.vertx.ext.web.templ.rythm;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rythm.impl.RythmTemplateEngineImpl;

/**
 * A template engine that uses the rythm library
 * @author Konstantin Volivach kostya05983@mail.ru
 */
@VertxGen
public interface RythmTemplateEngine extends TemplateEngine {

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static RythmTemplateEngine create() {
    return new RythmTemplateEngineImpl();
  }
}
