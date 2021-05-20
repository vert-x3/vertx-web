package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.net.SocketAddress;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.List;

public class ServerWebSocketWrapper implements ServerWebSocket {
  private final ServerWebSocket delegate;
  private final String host;
  private final String scheme;
  private final boolean isSsl;
  private final SocketAddress remoteAddress;

  public ServerWebSocketWrapper(ServerWebSocket delegate,
                                String host,
                                String scheme,
                                boolean isSsl,
                                SocketAddress remoteAddress) {
    this.delegate = delegate;
    this.host = host;
    this.scheme = scheme;
    this.isSsl = isSsl;
    this.remoteAddress = remoteAddress;
  }

  @Override
  public ServerWebSocket exceptionHandler(Handler<Throwable> handler) {
    delegate.exceptionHandler(handler);
    return this;
  }

  @Override
  public Future<Void> write(Buffer data) {
    return delegate.write(data);
  }

  @Override
  public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
    delegate.write(data, handler);
  }

  @Override
  public ServerWebSocket handler(Handler<Buffer> handler) {
    delegate.handler(handler);
    return this;
  }

  @Override
  public ServerWebSocket pause() {
    delegate.pause();
    return this;
  }

  @Override
  public ServerWebSocket resume() {
    delegate.resume();
    return this;
  }

  @Override
  public ServerWebSocket fetch(long amount) {
    delegate.fetch(amount);
    return this;
  }

  @Override
  public ServerWebSocket endHandler(Handler<Void> endHandler) {
    delegate.endHandler(endHandler);
    return this;
  }

  @Override
  public ServerWebSocket setWriteQueueMaxSize(int maxSize) {
    delegate.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return delegate.writeQueueFull();
  }

  @Override
  public ServerWebSocket drainHandler(Handler<Void> handler) {
    delegate.drainHandler(handler);
    return this;
  }

  @Override
  public String binaryHandlerID() {
    return delegate.binaryHandlerID();
  }

  @Override
  public String textHandlerID() {
    return delegate.textHandlerID();
  }

  @Override
  public String subProtocol() {
    return delegate.subProtocol();
  }

  @Override
  public Short closeStatusCode() {
    return delegate.closeStatusCode();
  }

  @Override
  public String closeReason() {
    return delegate.closeReason();
  }

  @Override
  public MultiMap headers() {
    return delegate.headers();
  }

  @Override
  public Future<Void> writeFrame(WebSocketFrame frame) {
    return delegate.writeFrame(frame);
  }

  @Override
  public ServerWebSocket writeFrame(WebSocketFrame frame, Handler<AsyncResult<Void>> handler) {
    delegate.writeFrame(frame, handler);
    return this;
  }

  @Override
  public Future<Void> writeFinalTextFrame(String text) {
    return delegate.writeFinalTextFrame(text);
  }

  @Override
  public ServerWebSocket writeFinalTextFrame(String text, Handler<AsyncResult<Void>> handler) {
    delegate.writeFinalTextFrame(text, handler);
    return this;
  }

  @Override
  public Future<Void> writeFinalBinaryFrame(Buffer data) {
    return delegate.writeFinalBinaryFrame(data);
  }

  @Override
  public ServerWebSocket writeFinalBinaryFrame(Buffer data, Handler<AsyncResult<Void>> handler) {
    delegate.writeFinalBinaryFrame(data, handler);
    return this;
  }

  @Override
  public Future<Void> writeBinaryMessage(Buffer data) {
    return delegate.writeBinaryMessage(data);
  }

  @Override
  public ServerWebSocket writeBinaryMessage(Buffer data, Handler<AsyncResult<Void>> handler) {
    delegate.writeBinaryMessage(data, handler);
    return this;
  }

  @Override
  public Future<Void> writeTextMessage(String text) {
    return delegate.writeTextMessage(text);
  }

  @Override
  public ServerWebSocket writeTextMessage(String text, Handler<AsyncResult<Void>> handler) {
    delegate.writeTextMessage(text, handler);
    return this;
  }

  @Override
  public WebSocketBase writePing(Buffer data, Handler<AsyncResult<Void>> handler) {
    delegate.writePing(data, handler);
    return this;
  }

  @Override
  public Future<Void> writePing(Buffer data) {
    return delegate.writePing(data);
  }

  @Override
  public WebSocketBase writePong(Buffer data, Handler<AsyncResult<Void>> handler) {
    delegate.writePong(data, handler);
    return this;
  }

  @Override
  public Future<Void> writePong(Buffer data) {
    return delegate.writePong(data);
  }

  @Override
  public ServerWebSocket closeHandler(Handler<Void> handler) {
    delegate.closeHandler(handler);
    return this;
  }

  @Override
  public ServerWebSocket frameHandler(Handler<WebSocketFrame> handler) {
    delegate.frameHandler(handler);
    return this;
  }

  @Override
  public WebSocketBase textMessageHandler(@Nullable Handler<String> handler) {
    delegate.textMessageHandler(handler);
    return this;
  }

  @Override
  public WebSocketBase binaryMessageHandler(@Nullable Handler<Buffer> handler) {
    delegate.binaryMessageHandler(handler);
    return this;
  }

  @Override
  public WebSocketBase pongHandler(@Nullable Handler<Buffer> handler) {
    delegate.pongHandler(handler);
    return this;
  }

  @Override
  public Future<Void> end() {
    return delegate.end();
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    delegate.end(handler);
  }

  @Override
  public @Nullable String scheme() {
    return scheme;
  }

  @Override
  public @Nullable String host() {
    return host;
  }

  @Override
  public String uri() {
    return delegate.uri();
  }

  @Override
  public String path() {
    return delegate.path();
  }

  @Override
  public @Nullable String query() {
    return delegate.query();
  }

  @Override
  public void accept() {
    delegate.accept();
  }

  @Override
  public void reject() {
    delegate.reject();
  }

  @Override
  public void reject(int status) {
    delegate.reject(status);
  }

  @Override
  public void setHandshake(Future<Integer> future, Handler<AsyncResult<Integer>> handler) {
    delegate.setHandshake(future, handler);
  }

  @Override
  public Future<Integer> setHandshake(Future<Integer> future) {
    return delegate.setHandshake(future);
  }

  @Override
  public Future<Void> close() {
    return delegate.close();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    delegate.close(handler);
  }

  @Override
  public Future<Void> close(short statusCode) {
    return delegate.close(statusCode);
  }

  @Override
  public void close(short statusCode, Handler<AsyncResult<Void>> handler) {
    delegate.close(statusCode, handler);
  }

  @Override
  public Future<Void> close(short statusCode, @Nullable String reason) {
    return delegate.close(statusCode, reason);
  }

  @Override
  public void close(short statusCode, @Nullable String reason, Handler<AsyncResult<Void>> handler) {
    delegate.close(statusCode, reason, handler);
  }

  @Override
  public SocketAddress remoteAddress() {
    return remoteAddress;
  }

  @Override
  public SocketAddress localAddress() {
    return delegate.localAddress();
  }

  @Override
  public boolean isSsl() {
    return isSsl;
  }

  @Override
  public boolean isClosed() {
    return delegate.isClosed();
  }

  @Override
  public SSLSession sslSession() {
    return delegate.sslSession();
  }

  @Override
  @Deprecated
  public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
    return delegate.peerCertificateChain();
  }

  @Override
  public List<Certificate> peerCertificates() throws SSLPeerUnverifiedException {
    return delegate.peerCertificates();
  }
}
