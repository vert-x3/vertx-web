package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
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
import java.io.IOException;
import java.io.PrintWriter;

import static junit.framework.TestCase.assertNotNull;

@RunWith(VertxUnitRunner.class)
public class RythmTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-rythm-template2.html", render -> {
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
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/test-rythm-template.html", render -> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!DOCTYPE html>\n" +
          "<html lang=\"en\">\n" +
          "<head>\n" +
          "<meta charset=\"UTF-8\">\n" +
          "<title>FS</title>\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "</body>\n" +
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject();

    engine.render(context, "nosuchtemplate.html", render -> {
      should.assertFalse(render.succeeded());
      test.complete();
    });
  }

  @Test
  public void testWithLocale(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("lang", "en-gb");

    engine.render(context, "somedir/test-rythm-template2.html", render -> {
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
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
  }

  @Test
  public void testEngine() {
    RythmTemplateEngine engine = RythmTemplateEngine.create(vertx);
    assertNotNull(engine.unwrap());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    final Async test = should.async();

    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".html", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("before", render.result().toString());
      // cache is enabled so if we change the content that should not affect the result

      try {
        PrintWriter out2 = new PrintWriter(temp);
        out2.print("after");
        out2.flush();
        out2.close();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), render2 -> {
        should.assertTrue(render2.succeeded());
        should.assertEquals("before", render2.result().toString());
        test.complete();
      });
    });
  }
}
