package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface ServiceResponse {

  Integer getStatusCode();

  String getStatusMessage();

  Buffer getPayload();

  MultiMap getHeaders();

  @Fluent
  ServiceResponse setHeaders(MultiMap headers);

  @Fluent
  ServiceResponse setStatusCode(Integer statusCode);

  @Fluent
  ServiceResponse setStatusMessage(String statusMessage);

  @Fluent
  ServiceResponse setPayload(Buffer payload);

  @Fluent
  ServiceResponse putHeader(String key, String value);

  static ServiceResponse completedWithJson(JsonObject jsonObject) {
    return completedWithJson(jsonObject.toBuffer());
  }

  static ServiceResponse completedWithJson(JsonArray jsonArray) {
    return completedWithJson(jsonArray.toBuffer());
  }

  static ServiceResponse completedWithJson(Buffer json) {
    ServiceResponse op = new ServiceResponse();
    op.setStatusCode(200);
    op.setStatusMessage("OK");
    op.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json");
    op.setPayload(json);
    return op;
  }

  static ServiceResponse completedWithPlainText(Buffer text) {
    return new ServiceResponse()
      .setStatusCode(200)
      .setStatusMessage("OK")
      .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
      .setPayload(text);
  }

  static ServiceResponse completedWithStream
}
