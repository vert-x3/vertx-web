package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rythm.RythmTemplateEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(VertxUnitRunner.class)
public class RythmTemplateTest {

  @Test
  public void testTemplateHandleOnClasspath(TestContext should) {
    final Async test = should.async();

    System.setProperty("vertxweb.environment", "development");
    TemplateEngine engine = RythmTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("somedir/test-rythm-template2.html").getFile());

    engine.render(context, file.getAbsolutePath(), render-> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!DOCTYPE html>\n" +
          "<html lang=\"en\">\n" +
          "<head>\n" +
          "<meta charset=\"UTF-8\">\n" +
          "<title>Title</title>\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "</body>\n" +
          "</html>";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

}
