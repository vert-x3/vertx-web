package io.vertx.ext.web.proxy.handler;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.ext.web.proxy.WebProxyTestBase;
import io.vertx.httpproxy.HttpProxy;
import org.junit.Test;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ProxyHandlerTest extends WebProxyTestBase {

  @Test
  public void shouldFailWithBodyHandlerOnPreviousMatchingRoute() throws Exception {
    HttpProxy proxy = HttpProxy.reverseProxy(proxyClient);
    proxy.origin(1234, "localhost");
    router.route().handler(BodyHandler.create());
    router.get("/path").handler(ProxyHandler.create(proxy));
    testRequest(HttpMethod.GET, "/path", 500, "Internal Server Error");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailWithBodyHandlerOnSameRoute() throws Exception {
    HttpProxy proxy = HttpProxy.reverseProxy(proxyClient);
    proxy.origin(1234, "localhost");
    router.get("/path")
      .handler(BodyHandler.create())
      .handler(ProxyHandler.create(proxy));
  }

  @Test
  public void shouldPassAuthHandlerOnSameRoute() throws Exception {
    HttpProxy proxy = HttpProxy.reverseProxy(proxyClient);
    proxy.origin(1234, "localhost");
    router.get("/path")
      .handler(SimpleAuthenticationHandler.create().authenticate(rc -> Future.succeededFuture(User.fromName("John"))))
      .handler(ProxyHandler.create(proxy));
    backendRouter.route(HttpMethod.GET, "/path").handler(rc -> {
      rc.response().setStatusCode(200);
      rc.response().setStatusMessage("statusMessage");
      rc.response().end("data");
    });
    testRequest(HttpMethod.GET, "/path", 200, "statusMessage", "data");
  }

  @Test
  public void testProxyHandler() throws Exception {
    HttpProxy proxy = HttpProxy.reverseProxy(proxyClient);
    proxy.origin(1234, "localhost");
    router.route(HttpMethod.GET, "/path").handler(ProxyHandler.create(proxy));
    backendRouter.route(HttpMethod.GET, "/path").handler(rc -> {
      rc.response().setStatusCode(200);
      rc.response().setStatusMessage("statusMessage");
      rc.response().end("data");
    });
    testRequest(HttpMethod.GET, "/path", 200, "statusMessage", "data");
  }

  @Test
  public void testProxyHandlerWithPortHost() throws Exception {
    HttpProxy proxy1 = HttpProxy.reverseProxy(proxyClient);
    router.route(HttpMethod.GET, "/path").handler(ProxyHandler.create(proxy1, 1234, "localhost"));
    backendRouter.route(HttpMethod.GET, "/path").handler(rc -> {
      rc.response().setStatusCode(200);
      rc.response().setStatusMessage("statusMessage");
      rc.response().end("data");
    });
    testRequest(HttpMethod.GET, "/path", 200, "statusMessage", "data");
  }
}
