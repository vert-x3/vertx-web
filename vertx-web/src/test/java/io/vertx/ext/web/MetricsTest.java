package io.vertx.ext.web;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.core.spi.observability.HttpRequest;
import io.vertx.core.spi.observability.HttpResponse;
import io.vertx.test.fakemetrics.*;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetricsTest extends WebTestBase {

  final FakeHttpServerMetrics fakeHttpServerMetrics = new FakeHttpServerMetrics();

  @Override
  public VertxOptions getOptions() {
    return new VertxOptions()
      .setMetricsOptions(
        new MetricsOptions()
          .setEnabled(true)
          .setFactory(options -> new VertxMetrics() {
            public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
              return fakeHttpServerMetrics;
            }
          }));
  }

  @Test
  public void testSimpleRoute() throws Exception {
    fakeHttpServerMetrics.reset(new String[] { null });
    // ensure that the metrics are called in the right order
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSimpleRouterMultipleRoutes() throws Exception {
    fakeHttpServerMetrics.reset("A", "B", "C", "D", "E");
    // ensure that the metrics are called in the right order
    router.route().setName("A").handler(RoutingContext::next);
    router.route().setName("B").handler(RoutingContext::next);
    router.route().setName("C").handler(RoutingContext::next);
    router.route().setName("D").handler(RoutingContext::next);
    router.route().setName("E").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSimpleRouterMultipleRoutesSomeSkiped() throws Exception {
    fakeHttpServerMetrics.reset("A", "B", "D", "E");
    // ensure that the metrics are called in the right order
    router.route().setName("A").handler(RoutingContext::next);
    router.route().setName("B").handler(RoutingContext::next);
    router.route("/skip-me").setName("C").handler(RoutingContext::next);
    router.route().setName("D").handler(RoutingContext::next);
    router.route().setName("E").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }
}

class FakeHttpServerMetrics extends FakeMetricsBase implements HttpServerMetrics<HttpServerMetric, WebSocketMetric, SocketMetric> {

  public FakeHttpServerMetrics() {
  }

  private final AtomicInteger seq = new AtomicInteger(0);
  private final AtomicInteger routed = new AtomicInteger(0);
  private String[] args;

  public void reset(String... args) {
    seq.set(0);
    routed.set(0);
    this.args = args == null ? new String[0] : args;
  }

  public HttpServerMetric requestBegin(SocketMetric socketMetric, HttpRequest request) {
    assertEquals(2, seq.incrementAndGet());
    return new HttpServerMetric(request, socketMetric);
  }

  public void requestEnd(HttpServerMetric requestMetric, HttpRequest request, long bytesRead) {
    assertEquals(5, seq.incrementAndGet());
    requestMetric.requestEnded.set(true);
    requestMetric.bytesRead.set(bytesRead);
  }

  public HttpServerMetric responsePushed(SocketMetric socketMetric, HttpMethod method, String uri, HttpResponse response) {
    HttpServerMetric requestMetric = new HttpServerMetric(uri, socketMetric);
    requestMetric.response.set(response);
    return requestMetric;
  }

  public void requestReset(HttpServerMetric requestMetric) {
    requestMetric.failed.set(true);
  }

  public void responseBegin(HttpServerMetric requestMetric, HttpResponse response) {
    assertEquals(3, seq.incrementAndGet());
    requestMetric.response.set(response);
  }

  public void responseEnd(HttpServerMetric requestMetric, HttpResponse response, long bytesWritten) {
    assertEquals(4, seq.incrementAndGet());
    requestMetric.responseEnded.set(true);
    requestMetric.bytesWritten.set(bytesWritten);
  }

  public SocketMetric connected(SocketAddress remoteAddress, String remoteName) {
    assertEquals(1, seq.incrementAndGet());
    return new SocketMetric(remoteAddress, remoteName);
  }

  public void disconnected(SocketMetric socketMetric, SocketAddress remoteAddress) {
    assertEquals(6, seq.incrementAndGet());
    socketMetric.connected.set(false);
  }

  public void bytesRead(SocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    socketMetric.bytesRead.addAndGet(numberOfBytes);
    socketMetric.bytesReadEvents.add(numberOfBytes);
  }

  public void bytesWritten(SocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    socketMetric.bytesWritten.addAndGet(numberOfBytes);
    socketMetric.bytesWrittenEvents.add(numberOfBytes);
  }

  public void exceptionOccurred(SocketMetric socketMetric, SocketAddress remoteAddress, Throwable t) {
  }

  public void requestRouted(HttpServerMetric requestMetric, String route) {
    assertEquals(args[routed.getAndIncrement()], route);
    requestMetric.route.set(route);
  }
}
