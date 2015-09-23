package io.vertx.ext.web.templ;

import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;
import io.vertx.ext.web.impl.Utils;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public class GStringTemplateTest extends GroovyTemplateTestBase {

  @Override
  protected TemplateEngine createTemplateEngine() {
    return new GStringTemplateEngine(Utils.getClassLoader());
  }

  @Override
  protected String getFSTemplateName() {
    return "test-gstring-template3.gtpl";
  }

  @Override
  protected String getCpTemplateName() {
    return "test-gstring-template2.gtpl";
  }

  @Override
  protected String getExtension() {
    return "gtpl";
  }

  @Override
  protected String getFSExpectedResult() {
    return "Hello from FS, badger";
  }

  @Override
  protected String getCpExpectedResult() {
    return "Hello from CP, badger";
  }

  @Override
  protected String getAnotherExtensionExpectedResult() {
    return "Hello from CP, fox";
  }
}
