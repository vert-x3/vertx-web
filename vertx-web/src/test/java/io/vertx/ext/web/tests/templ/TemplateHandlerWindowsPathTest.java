package io.vertx.ext.web.tests.templ;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.Test;

import java.util.Map;

public class TemplateHandlerWindowsPathTest extends WebTestBase {

  @Test
  public void testWildcardRoute() throws Exception {
    RecordingTemplateFileNameEngine engine = new RecordingTemplateFileNameEngine();
    router.route("/templates/*").handler(TemplateHandler.create(engine, "templates", "text/html"));
    testRequest(HttpMethod.GET, "/templates/..\\outside.html", 200, "OK");
    assertEquals("templates/outside.html", engine.lastTemplateFileName);
  }

  @Test
  public void testRegexRoute() throws Exception {
    RecordingTemplateFileNameEngine engine = new RecordingTemplateFileNameEngine();
    router.getWithRegex(".+\\.html").handler(TemplateHandler.create(engine, "templates", "text/html"));
    testRequest(HttpMethod.GET, "/..\\outside.html", 200, "OK");
    assertEquals("templates/outside.html", engine.lastTemplateFileName);
  }

  @Test
  public void testPathParamRoute() throws Exception {
    RecordingTemplateFileNameEngine engine = new RecordingTemplateFileNameEngine();
    router.get("/templates/:name").handler(TemplateHandler.create(engine, "templates", "text/html"));
    testRequest(HttpMethod.GET, "/templates/..\\outside.html", 200, "OK");
    assertEquals("templates/outside.html", engine.lastTemplateFileName);
  }

  @Test
  public void testPathTraversalAttemptWithBackslash() throws Exception {
    RecordingTemplateFileNameEngine engine = new RecordingTemplateFileNameEngine();
    router.getWithRegex(".+\\.html").handler(TemplateHandler.create(engine, "templates", "text/html"));
    testRequest(HttpMethod.GET, "/..\\..\\outside.html", 200, "OK");
    assertEquals("templates/outside.html", engine.lastTemplateFileName);
  }

  private static class RecordingTemplateFileNameEngine implements TemplateEngine {

    String lastTemplateFileName;

    @Override
    public Future<Buffer> render(Map<String, Object> context, String templateFileName) {
      this.lastTemplateFileName = templateFileName;
      return Future.succeededFuture(Buffer.buffer());
    }

    @Override
    public void clearCache() {
    }
  }
}
