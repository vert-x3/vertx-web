package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
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
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-rythm-template2.html", should.asyncAssertSuccess(render -> {
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

      should.assertEquals(expected, normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/test-rythm-template.html", should.asyncAssertSuccess(render -> {
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

      should.assertEquals(expected, normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject();

    engine.render(context, "nosuchtemplate.html", should.asyncAssertFailure());
  }

  @Test
  public void testWithLocale(TestContext should) {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("lang", "en-gb");

    engine.render(context, "somedir/test-rythm-template2.html", should.asyncAssertSuccess(render -> {
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

      should.assertEquals(expected, normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testEngine() {
    RythmTemplateEngine engine = RythmTemplateEngine.create(vertx);
    assertNotNull(engine.unwrap());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".html", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("before", normalizeCRLF(render.toString()));
      // cache is enabled so if we change the content that should not affect the result

      try (PrintWriter out2 = new PrintWriter(temp)) {
        out2.print("after");
        out2.flush();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("before", normalizeCRLF(render2.toString()));
      }));
    }));
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
