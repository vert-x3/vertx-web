package io.vertx.ext.apex.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.ApexTestBase;
import org.junit.Test;

/**
 * @author <a href="http://github.com/aesteve">Arnaud Esteve</a>
 */
public class StaticHandlerOnPathTest extends ApexTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create();
    router.route("/assets").handler(stat);
  }

  @Test
  public void testRoutePathRemoved() throws Exception {
    testRequest(HttpMethod.GET, "/assets/somedir/something.html", 200, "OK", "<html><body>Blah page</body></html>");
  }

}
