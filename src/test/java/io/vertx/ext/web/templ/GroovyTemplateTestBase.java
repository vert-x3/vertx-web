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

package io.vertx.ext.web.templ;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public abstract class GroovyTemplateTestBase extends WebTestBase {

  private groovy.text.TemplateEngine groovyEngine;
  private GroovyTemplateEngine engine;

  abstract protected groovy.text.TemplateEngine createTemplateEngine();

  abstract protected String getFSTemplateName();

  abstract protected String getCpTemplateName();

  abstract protected String getExtension();

  abstract protected String getFSExpectedResult();

  abstract protected String getCpExpectedResult();

  abstract protected String getAnotherExtensionExpectedResult();

  @Before
  public void initTemplateEngine() {
    groovyEngine = createTemplateEngine();
    engine = GroovyTemplateEngine.create(groovyEngine);
  }

  @Test
  public void testTemplateOnClasspath() throws Exception {
    testTemplateHandler(engine, "somedir", getCpTemplateName(), getCpExpectedResult());
  }

  @Test
  public void testTemplateOnFileSystem() throws Exception {
    testTemplateHandler(engine, "src/test/filesystemtemplates", getFSTemplateName(), getFSExpectedResult());
  }

  @Test
  public void testTemplateNoExtension() throws Exception {
    testTemplateHandler(engine, "somedir", withoutExtension(getCpTemplateName()), getCpExpectedResult());
  }

  @Test
  public void testTemplateChangeExtension() throws Exception {
    engine.setExtension("ztpl");
    testTemplateHandler(engine, "somedir", withoutExtension(getCpTemplateName()), getAnotherExtensionExpectedResult());
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    router.route().handler(TemplateHandler.create(engine, "somedir", "text/html"));
    testRequest(HttpMethod.GET, "/foo.gtpl", 500, "Internal Server Error");
  }

  @Test
  public void testGetGroovyEngine() throws Exception {
    assertEquals(groovyEngine, engine.getGroovyEngine());
  }

  private String withoutExtension(String tplName) {
    return tplName.split("." + getExtension())[0];
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName, String expected) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }


}
