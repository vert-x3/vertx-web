package io.vertx.ext.web.templ;

import groovy.text.StreamingTemplateEngine;
import groovy.text.TemplateEngine;
import io.vertx.ext.web.impl.Utils;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public class StreamingTemplateTest extends GroovyTemplateTestBase {

  @Override
  protected TemplateEngine createTemplateEngine() {
    return new StreamingTemplateEngine(Utils.getClassLoader());
  }

  @Override
  protected String getFSTemplateName() {
    return "test-streaming-template3.gtpl";
  }

  @Override
  protected String getCpTemplateName() {
    return "test-streaming-template2.gtpl";
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
