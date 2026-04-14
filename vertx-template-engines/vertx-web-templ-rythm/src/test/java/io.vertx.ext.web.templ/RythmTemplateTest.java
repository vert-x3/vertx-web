package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rythm.RythmTemplateEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

public class RythmTemplateTest {

  private static Vertx vertx;

  @BeforeAll
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath() {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-rythm-template2.html").await();
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

    assertEquals(expected, normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerOnFileSystem() {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "src/test/filesystemtemplates/test-rythm-template.html").await();
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

    assertEquals(expected, normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching() {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath();
  }

  @Test
  public void testNoSuchTemplate() {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject();

    assertThrows(Exception.class, () -> engine.render(context, "nosuchtemplate.html").await());
  }

  @Test
  public void testWithLocale() {
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("lang", "en-gb");

    Buffer render = engine.render(context, "somedir/test-rythm-template2.html").await();
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

    assertEquals(expected, normalizeCRLF(render.toString()));
  }

  @Test
  public void testEngine() {
    RythmTemplateEngine engine = RythmTemplateEngine.create(vertx);
    assertNotNull(engine.unwrap());
  }

  @Test
  public void testCachingEnabled() throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = RythmTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".html", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    Buffer render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
    // cache is enabled so if we change the content that should not affect the result

    try (PrintWriter out2 = new PrintWriter(temp)) {
      out2.print("after");
      out2.flush();
    }

    render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
