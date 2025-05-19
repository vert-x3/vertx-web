package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class WebProxyExamples {

  public void origin(Vertx vertx) {
    HttpServer backendServer = vertx.createHttpServer();

    Router backendRouter = Router.router(vertx);

    backendRouter.route(HttpMethod.GET, "/foo").handler(rc -> {
      rc.response()
        .putHeader("content-type", "text/html")
        .end("<html><body><h1>I'm the target resource!</h1></body></html>");
    });

    backendServer.requestHandler(backendRouter).listen(7070);
  }

  public void proxy(Vertx vertx) {
    HttpServer proxyServer = vertx.createHttpServer();

    Router proxyRouter = Router.router(vertx);

    proxyServer.requestHandler(proxyRouter);

    proxyServer.listen(8080);
  }

  public void route(Vertx vertx, Router proxyRouter) {
    HttpClient proxyClient = vertx.createHttpClient();

    HttpProxy httpProxy = HttpProxy.reverseProxy(proxyClient);
    httpProxy.origin(7070, "localhost");

    proxyRouter
      .route(HttpMethod.GET, "/foo").handler(ProxyHandler.create(httpProxy));
  }

  public void routeShort(Vertx vertx, Router proxyRouter) {
    HttpClient proxyClient = vertx.createHttpClient();

    HttpProxy httpProxy = HttpProxy.reverseProxy(proxyClient);

    proxyRouter
      .route(HttpMethod.GET, "/foo")
      .handler(ProxyHandler.create(httpProxy, 7070, "localhost"));
  }

  public void notCompatibleWithBodyHandler(Router router, HttpProxy productServiceProxy) {
    router.post().handler(BodyHandler.create()); // Don't do this
    router.route("/product").handler(ProxyHandler.create(productServiceProxy));
  }

  public void multi(Vertx vertx, Router proxyRouter) {
    HttpClient proxyClient = vertx.createHttpClient();

    HttpProxy httpProxy1 = HttpProxy.reverseProxy(proxyClient);
    httpProxy1.origin(7070, "localhost");

    HttpProxy httpProxy2 = HttpProxy.reverseProxy(proxyClient);
    httpProxy2.origin(6060, "localhost");

    proxyRouter
      .route(HttpMethod.GET, "/foo").handler(ProxyHandler.create(httpProxy1));

    proxyRouter
      .route(HttpMethod.GET, "/bar").handler(ProxyHandler.create(httpProxy2));
  }

}


