package io.vertx.ext.web.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.AllowForwardHeaders;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

class HttpServerRequestWrapper implements HttpServerRequest {

  private final HttpServerRequest delegate;
  private final ForwardedParser forwardedParser;

  private boolean modified;

  private HttpMethod method;
  private String path;
  private String query;
  private String uri;
  private String absoluteURI;
  private MultiMap params;

  HttpServerRequestWrapper(HttpServerRequest request, AllowForwardHeaders allowForward) {
    delegate = request;
    forwardedParser = new ForwardedParser(delegate, allowForward);
  }

  void changeTo(HttpMethod method, String uri) {
    modified = true;
    this.method = method;
    this.uri = uri;
    // lazy initialization
    this.path = null;
    this.query = null;
    this.absoluteURI = null;

    // parse
    int queryIndex = uri.indexOf('?');

    // there's a query
    if (queryIndex != -1) {
      int fragmentIndex = uri.indexOf('#', queryIndex);
      path = uri.substring(0, queryIndex);
      // there's a fragment
      if (fragmentIndex != -1) {
        query = uri.substring(queryIndex + 1, fragmentIndex);
      } else {
        query = uri.substring(queryIndex + 1);
      }
    } else {
      int fragmentIndex = uri.indexOf('#');
      // there's a fragment
      if (fragmentIndex != -1) {
        path = uri.substring(0, fragmentIndex);
      } else {
        path = uri;
      }
    }
  }

  @Override
  public Context context() {
    return delegate.context();
  }

  @Override
  public HttpServerRequest body(Handler<AsyncResult<Buffer>> handler) {
    delegate.body(handler);
    return this;
  }

  @Override
  public Future<Buffer> body() {
    return delegate.body();
  }

  @Override
  public long bytesRead() {
    return delegate.bytesRead();
  }

  @Override
  public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
    delegate.exceptionHandler(handler);
    return this;
  }

  @Override
  public HttpServerRequest handler(Handler<Buffer> handler) {
    delegate.handler(handler);
    return this;
  }

  @Override
  public HttpServerRequest pause() {
    delegate.pause();
    return this;
  }

  @Override
  public HttpServerRequest resume() {
    delegate.resume();
    return this;
  }

  @Override
  public HttpServerRequest fetch(long amount) {
    delegate.fetch(amount);
    return this;
  }

  @Override
  public HttpServerRequest endHandler(Handler<Void> handler) {
    delegate.endHandler(handler);
    return this;
  }

  @Override
  public HttpVersion version() {
    return delegate.version();
  }

  @Override
  public HttpMethod method() {
    if (!modified) {
      return delegate.method();
    }
    return method;
  }

  @Override
  public String uri() {
    if (!modified) {
      return delegate.uri();
    }
    return uri;
  }

  @Override
  public String path() {
    if (!modified) {
      return delegate.path();
    }
    return path;
  }

  @Override
  public String query() {
    if (!modified) {
      return delegate.query();
    }
    return query;
  }

  @Override
  public MultiMap params() {
    if (!modified) {
      return delegate.params();
    }
    if (params == null) {
      params = MultiMap.caseInsensitiveMultiMap();
      // if there is no query it's not really needed to parse it
      if (query != null) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> prms = queryStringDecoder.parameters();
        if (!prms.isEmpty()) {
          for (Map.Entry<String, List<String>> entry: prms.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
          }
        }
      }
    }

    return params;
  }

  @Override
  public String getParam(String param) {
    if (!modified) {
      return delegate.getParam(param);
    }

    return params().get(param);
  }

  @Override
  public HttpServerResponse response() {
    return delegate.response();
  }

  @Override
  public MultiMap headers() {
    return delegate.headers();
  }

  @Override
  public String getHeader(String s) {
    return delegate.getHeader(s);
  }

  @Override
  public String getHeader(CharSequence charSequence) {
    return delegate.getHeader(charSequence);
  }

  @Override
  public SocketAddress remoteAddress() {
    return forwardedParser.remoteAddress();
  }

  @Override
  public SocketAddress localAddress() {
    return delegate.localAddress();
  }

  @Override
  public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
    return delegate.peerCertificateChain();
  }

  @Override
  public SSLSession sslSession() {
    return delegate.sslSession();
  }

  @Override
  public String absoluteURI() {
    if (!modified) {
      return forwardedParser.absoluteURI();
    } else {
      if (absoluteURI == null) {
        String scheme = forwardedParser.scheme();
        String host = forwardedParser.host();

        // if both are not null we can rebuild the uri
        if (scheme != null && host != null) {
          absoluteURI = scheme + "://" + host + uri;
        } else {
          absoluteURI = uri;
        }
      }

      return absoluteURI;
    }
  }

  @Override
  public String scheme() {
    return forwardedParser.scheme();
  }

  @Override
  public String host() {
    return forwardedParser.host();
  }

  @Override
  public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
    delegate.customFrameHandler(handler);
    return this;
  }

  @Override
  public HttpConnection connection() {
    return delegate.connection();
  }

  @Override
  public HttpServerRequest bodyHandler(Handler<Buffer> handler) {
    delegate.bodyHandler(handler);
    return this;
  }

  @Override
  public void toNetSocket(Handler<AsyncResult<NetSocket>> handler) {
    delegate.toNetSocket(handler);
  }

  @Override
  public Future<NetSocket> toNetSocket() {
    return delegate.toNetSocket();
  }

  @Override
  public HttpServerRequest setExpectMultipart(boolean b) {
    delegate.setExpectMultipart(b);
    return this;
  }

  @Override
  public boolean isExpectMultipart() {
    return delegate.isExpectMultipart();
  }

  @Override
  public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> handler) {
    delegate.uploadHandler(handler);
    return this;
  }

  @Override
  public MultiMap formAttributes() {
    return delegate.formAttributes();
  }

  @Override
  public String getFormAttribute(String s) {
    return delegate.getFormAttribute(s);
  }

  @Override
  public int streamId() {
    return delegate.streamId();
  }

  @Override
  public void toWebSocket(Handler<AsyncResult<ServerWebSocket>> handler) {
    delegate.toWebSocket(toWebSocket -> {
      if (toWebSocket.succeeded()) {
        handler.handle(Future.succeededFuture(
          new ServerWebSocketWrapper(toWebSocket.result(), host(), scheme(), isSSL(), remoteAddress())));
      } else {
        handler.handle(toWebSocket);
      }
    });
  }

  @Override
  public Future<ServerWebSocket> toWebSocket() {
    return delegate.toWebSocket()
      .map(ws -> new ServerWebSocketWrapper(ws, host(), scheme(), isSSL(), remoteAddress()));
  }

  @Override
  public boolean isEnded() {
    return delegate.isEnded();
  }

  @Override
  public boolean isSSL() {
    return forwardedParser.isSSL();
  }

  @Override
  public HttpServerRequest streamPriorityHandler(Handler<StreamPriority> handler) {
    delegate.streamPriorityHandler(handler);
    return this;
  }

  @Override
  public StreamPriority streamPriority() {
    return delegate.streamPriority();
  }

  @Override
  public @Nullable Cookie getCookie(String name) {
    return delegate.getCookie(name);
  }

  @Override
  public int cookieCount() {
    return delegate.cookieCount();
  }

  @Override
  public Map<String, Cookie> cookieMap() {
    return delegate.cookieMap();
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    delegate.end(handler);
  }

  @Override
  public Future<Void> end() {
    return delegate.end();
  }

  @Override
  public HttpServerRequest routed(String route) {
    delegate.routed(route);
    return this;
  }

  @Override
  public Context context() {
    return delegate.context();
  }

  @Override
  public Pipe<Buffer> pipe() {
    return delegate.pipe();
  }

  @Override
  public Future<Void> pipeTo(WriteStream<Buffer> dst) {
    return delegate.pipeTo(dst);
  }

  @Override
  public void pipeTo(WriteStream<Buffer> dst, Handler<AsyncResult<Void>> handler) {
    delegate.pipeTo(dst, handler);
  }
}
