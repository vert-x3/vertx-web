package io.vertx.ext.web.templ;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import org.junit.Test;
/**
 * Created by jay on 5/17/17.
 */
public class MustacheTemplateTest extends WebTestBase {
    @Override
    public void setUp() throws Exception {
        System.setProperty("vertx.disableFileCaching", "true");
        super.setUp();
    }

    @Test
    public void testTemplateHandler() throws Exception {
        TemplateEngine engine = MustacheTemplateEngine.create();
        testTemplateHandler(engine, "src/test/filesystemtemplates", "test", "{bar=fox, foo=badger, john=doe}");
    }

    @Test
    public void testTemplateHandler2() throws Exception {
        TemplateEngine engine = MustacheTemplateEngine.create();
        testTemplateHandler2(engine, "src/test/filesystemtemplates", "test", "{title=Lorem}");
    }

    private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName,
                                     String expected) throws Exception {
        router.route().handler(context -> {
            context.put("foo",  "badger");
            context.put("bar",  "fox");
            context.put("john", "doe");
            context.next();
        });
        router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
        testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
    }

    private void testTemplateHandler2(TemplateEngine engine, String directoryName, String templateName,
                                     String expected) throws Exception {
        router.route().handler(context -> {
            context.put("title", "Lorem");
            context.next();
        });
        router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
        testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
    }
}
