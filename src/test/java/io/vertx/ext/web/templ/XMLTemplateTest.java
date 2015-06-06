package io.vertx.ext.web.templ;

import groovy.text.TemplateEngine;
import groovy.text.XmlTemplateEngine;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public class XMLTemplateTest extends GroovyTemplateTestBase {

  @Override
  protected TemplateEngine createTemplateEngine() {
    try {
      return new XmlTemplateEngine();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getFSTemplateName() {
    return "test-xml-template3.gtpl";
  }

  @Override
  protected String getCpTemplateName() {
    return "test-xml-template2.gtpl";
  }

  @Override
  protected String getExtension() {
    return "gtpl";
  }

  @Override
  protected String getFSExpectedResult() {
    return getResult("badger", "How are you today?");
  }

  @Override
  protected String getCpExpectedResult() {
    return getResult("fox", "How are you today?");
  }

  @Override
  protected String getAnotherExtensionExpectedResult() {
    return getResult("fox", "How were you yesterday?");
  }

  private String getResult(String name, String lastPhrase) {
    return "<document type='letter'>\n  " + name + "est\n" + "  <foo:to xmlns:foo='baz'>\n    " + name + " &quot;" + name + "&quot; " + name + "\n  </foo:to>\n  " + lastPhrase + "\n" + "</document>\n";
  }
}
