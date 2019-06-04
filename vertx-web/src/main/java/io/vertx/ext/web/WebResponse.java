package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.impl.WebResponseImpl;

public interface WebResponse {

  @Fluent
  WebResponse putHeader(String key, String value);

  @Fluent
  WebResponse setHeaders(MultiMap headers);

  @Fluent
  WebResponse setStatusCode(Integer statusCode);

  @Fluent
  WebResponse setStatusMessage(String statusMessage);

  @Fluent
  WebResponse setPayload(Buffer payload);

  @Fluent
  WebResponse setStream(ReadStream<Buffer> buffer);

  static WebResponse create() {
    return new WebResponseImpl();
  }

  static WebResponse fromJson(JsonObject jsonObject) {
    return fromJson(jsonObject.toBuffer());
  }

  static WebResponse fromJson(JsonArray jsonArray) {
    return fromJson(jsonArray.toBuffer());
  }

  static WebResponse fromJson(Buffer json) {
    WebResponse op = new WebResponseImpl();
    op.setStatusCode(200);
    op.setStatusMessage("OK");
    op.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json");
    op.setPayload(json);
    return op;
  }

  static WebResponse fromPlainText(Buffer text) {
    return new WebResponseImpl()
      .setStatusCode(200)
      .setStatusMessage("OK")
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
      .setPayload(text);
  }

  static WebResponse fromBuffer(String contentType, Buffer buffer) {
    return new WebResponseImpl()
      .setStatusCode(200)
      .setStatusMessage("OK")
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), contentType)
      .setPayload(buffer);
  }

  static WebResponse fromStream(String contentType, ReadStream<Buffer> readStream) {
    return new WebResponseImpl()
      .setStatusCode(200)
      .setStatusMessage("OK")
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), contentType)
      .setStream(readStream);
  }
}
