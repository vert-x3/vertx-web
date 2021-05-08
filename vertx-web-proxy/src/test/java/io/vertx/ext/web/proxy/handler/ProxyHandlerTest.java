package io.vertx.ext.web.proxy.handler;

import io.vertx.core.http.HttpMethod;

import io.vertx.ext.web.proxy.WebProxyTestBase;
import io.vertx.httpproxy.HttpProxy;
import org.junit.Test;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ProxyHandlerTest extends WebProxyTestBase {

  @Test
  public void testProxyHandler() throws Exception {
    HttpProxy backend = HttpProxy.reverseProxy2(proxyClient);
    backend.target(1234, "localhost");
    router.route(HttpMethod.GET, "/path").handler(ProxyHandler.create(backend));
    backendRouter.route(HttpMethod.GET, "/path").handler(rc -> {
      rc.response().setStatusCode(200);
      rc.response().setStatusMessage("statusMessage");
      rc.response().end("data");
    });
    testRequest(HttpMethod.GET, "/path", 200, "statusMessage", "data");
  }

  @Test
  public void testProxyHandlerWithPortHost() throws Exception {
    HttpProxy backend1 = HttpProxy.reverseProxy2(proxyClient);
    router.route(HttpMethod.GET, "/path").handler(ProxyHandler.create(backend1, 1234, "localhost"));
    backendRouter.route(HttpMethod.GET, "/path").handler(rc -> {
      rc.response().setStatusCode(200);
      rc.response().setStatusMessage("statusMessage");
      rc.response().end("data");
    });
    testRequest(HttpMethod.GET, "/path", 200, "statusMessage", "data");
  }
}
