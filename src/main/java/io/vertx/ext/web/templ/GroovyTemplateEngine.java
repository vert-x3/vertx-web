package io.vertx.ext.web.templ;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.templ.impl.GroovyTemplateEngineImpl;

/**
 * A simple wrapper for Groovy template engines to be used as
 * Vert.x template engines
 *
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
@VertxGen
public interface GroovyTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "gtpl";

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static GroovyTemplateEngine create(groovy.text.TemplateEngine groovyEngine) {
    return new GroovyTemplateEngineImpl(groovyEngine);
  }

  /**
   * Set the extension for the engine
   *
   * @param extension the extension
   * @return a reference to this for fluency
   */
  GroovyTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize the maxCacheSize
   * @return a reference to this for fluency
   */
  GroovyTemplateEngine setMaxCacheSize(int maxCacheSize);

  /**
   * Get a reference to the Groovy template engine
   *
   * @return a reference to the internal Groovy groovy.text.TemplateEngine
   */
  @GenIgnore
  groovy.text.TemplateEngine getGroovyEngine();
}
