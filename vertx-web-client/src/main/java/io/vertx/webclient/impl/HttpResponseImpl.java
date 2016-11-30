package io.vertx.webclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
  public HttpClientResponse httpClientResponse() {
    return resp;
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
    return buff;
  }

  @Override
  public String bodyAsString() {
    return HttpResponseTemplateImpl.utf8Unmarshaller.apply(buff);
  }

  @Override
  public String bodyAsString(String encoding) {
    return buff.toString(encoding);
  }

  @Override
  public JsonObject bodyAsJsonObject() {
    return HttpResponseTemplateImpl.jsonObjectUnmarshaller.apply(buff);
  }

  @Override
  public <R> R bodyAs(Class<R> type) {
    return HttpResponseTemplateImpl.jsonUnmarshaller(type).apply(buff);
  }

  @Override
  public void bufferBody(Handler<AsyncResult<Buffer>> handler) {
    Future<Buffer> fut = Future.future();
    fut.setHandler(handler);
    if (buff != null) {
      fut.complete(buff);
    } else {
      resp.exceptionHandler(err -> {
        if (!fut.isComplete()) {
          fut.fail(err);
        }
      });
      resp.bodyHandler(buff -> {
        if (!fut.isComplete()) {
          fut.complete(buff);
        }
      });
    }
  }
}
