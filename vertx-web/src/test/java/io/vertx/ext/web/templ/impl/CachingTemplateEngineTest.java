package io.vertx.ext.web.templ.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="https://github.com/mystdeim">Roman Novikov</a>
 * @since 4/1/17.
 */
public class CachingTemplateEngineTest {

  CachingTemplateEngineImpl engine;

  @Before
  public void setUp() {
    engine = new CachingTemplateEngineImpl(".jade");
  }

  @Test
  public void testAdjustLocation_Empty() {
    assertEquals("templates/index.jade", engine.adjustLocation("templates/"));
  }

  @Test
  public void testAdjustLocation_Ext() {
    assertEquals("templates/.jade", engine.adjustLocation("templates/.jade"));
  }

  @Test
  public void testAdjustLocation_About() {
    assertEquals("templates/about.jade", engine.adjustLocation("templates/about.jade"));
  }

  private static class CachingTemplateEngineImpl extends CachingTemplateEngine<String> {

    CachingTemplateEngineImpl(String ext) {
      super(ext, 1);
    }

    @Override
    protected String adjustLocation(String location) {
      return super.adjustLocation(location);
    }

    @Override
    public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {}
  }
}
