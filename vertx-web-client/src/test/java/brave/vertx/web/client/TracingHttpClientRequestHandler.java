package brave.vertx.web.client;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpClientAdapter;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.impl.HttpContext;

final class TracingHttpClientRequestHandler implements Handler<HttpContext<?>> {
  static final Setter<HttpClientRequest, String> SETTER = new Setter<HttpClientRequest, String>() {
    @Override public void put(HttpClientRequest carrier, String key, String value) {
      carrier.putHeader(key, value);
    }

    @Override public String toString() {
      return "HttpClientRequest::putHeader";
    }
  };

  final Tracer tracer;
  final HttpClientHandler<HttpClientRequest, HttpClientResponse> handler;
  final TraceContext.Injector<HttpClientRequest> injector;
  final String serverName;

  TracingHttpClientRequestHandler(HttpTracing httpTracing) {
    tracer = httpTracing.tracing().tracer();
    handler = HttpClientHandler.create(httpTracing, new Adapter());
    injector = httpTracing.tracing().propagation().injector(SETTER);
    serverName = httpTracing.serverName();
  }

  @Override public void handle(HttpContext<?> context) {
    Span span = context.get(Span.class.getName());
    switch (context.phase()) {
      case SEND_REQUEST:
        // TODO: the phases seem to have a small glitch from a state machine POV
        // we have a "sneaky response" in the case of a redirect. Ex a call with one redirect:
        //
        // PREPARE_REQUEST
        // SEND_REQUEST
        // SEND_REQUEST
        // RECEIVE_RESPONSE
        // DISPATCH_RESPONSE
        //
        // Seems we'd want to be notified on RECEIVE_RESPONSE as opposed to heuristics because
        // sometimes a reentrant callback feels like a bug. Another way is for us to track the
        // redirect count and assume if we get SEND_RESPONSE before RECEIVE_RESPONSE and the
        // redirectCount incremented, it was a redirect. All this said, it seems nicer to qualify
        // the phases in such a way that in redirect we have a receive for every send. If not, a
        // "receive like" phase for when we are in redirect. ex PREPARE_REDIRECT?
        if (span != null) finishSpan(context, span); // assume redirect
        span = handler.handleSend(injector, context.clientRequest());
        context.set(Span.class.getName(), span);
        break;
      case DISPATCH_RESPONSE:
      case FAILURE:
        assert span != null : "unexpected lifecycle";
        if (span == null) break; // don't break
        finishSpan(context, span);
        context.set(Span.class.getName(), null);
        span = null;
        break;
    }

    // TODO: test that this is readable ex trace IDs in logs
    if (span != null) {
      try (SpanInScope ws = tracer.withSpanInScope(span)) {
        context.next();
      } catch (RuntimeException | Error e) {
        span.error(e); // TODO: test that other interceptors having an error end up in the span
        context.set(Span.class.getName(), null);
        throw e;
      }
    } else {
      context.next();
    }
  }

  private void finishSpan(HttpContext<?> context, Span span) {
    parseConnectionAddress(context.clientRequest(), span);
    handler.handleReceive(context.clientResponse(), context.failure(), span);
  }

  static void parseConnectionAddress(HttpClientRequest request, Span span) {
    if (span.isNoop()) return;
    HttpConnection connection = request.connection();
    if (connection == null) return;
    SocketAddress address = connection.remoteAddress();
    if (address != null) {
      if (span.remoteIpAndPort(address.host(), address.port())) return;
    }
  }

  static final class Adapter extends HttpClientAdapter<HttpClientRequest, HttpClientResponse> {

    @Override public String method(HttpClientRequest request) {
      return request.method().name();
    }

    @Override public String path(HttpClientRequest request) {
      return request.path();
    }

    @Override public String url(HttpClientRequest request) {
      return request.absoluteURI();
    }

    @Override public String requestHeader(HttpClientRequest request, String name) {
      return request.headers().get(name);
    }

    @Override public Integer statusCode(HttpClientResponse response) {
      return statusCodeAsInt(response);
    }

    @Override public int statusCodeAsInt(HttpClientResponse response) {
      return response.statusCode();
    }
  }
}
