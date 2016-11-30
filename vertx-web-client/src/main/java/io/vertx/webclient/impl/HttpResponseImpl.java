package io.vertx.webclient.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.HttpResponse;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpResponseImpl<T> implements HttpResponse<T> {

  private final HttpClientResponse resp;
  private Buffer buff;
  private T body;

  HttpResponseImpl(HttpClientResponse resp, Buffer buff, T body) {
    this.resp = resp;
    this.buff = buff;
    this.body = body;
  }

  @Override
  public HttpVersion version() {
    return resp.version();
  }

  @Override
  public int statusCode() {
    return resp.statusCode();
  }

  @Override
  public String statusMessage() {
    return resp.statusMessage();
  }

  @Override
  public String getHeader(String headerName) {
    return resp.getHeader(headerName);
  }

  @Override
  public MultiMap trailers() {
    return resp.trailers();
  }

  @Override
  public String getTrailer(String trailerName) {
    return resp.getTrailer(trailerName);
  }

  @Override
  public List<String> cookies() {
    return resp.cookies();
  }

  @Override
  public MultiMap headers() {
    return resp.headers();
  }

  @Override
  public T body() {
    return body;
  }

  @Override
  public Buffer bodyAsBuffer() {
    return buff != null ? buff : body instanceof Buffer ? (Buffer)body : null;
  }

  @Override
  public String bodyAsString() {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.utf8Unmarshaller.apply(b) : null;
  }

  @Override
  public String bodyAsString(String encoding) {
    Buffer b = bodyAsBuffer();
    return b != null ? b.toString(encoding) : null;
  }

  @Override
  public JsonObject bodyAsJsonObject() {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.jsonObjectUnmarshaller.apply(b) : null;
  }

  @Override
  public <R> R bodyAs(Class<R> type) {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.jsonUnmarshaller(type).apply(b) : null;
  }
}
