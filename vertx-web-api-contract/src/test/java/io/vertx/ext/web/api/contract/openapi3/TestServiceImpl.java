package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

public class TestServiceImpl implements TestService {
  Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }


  @Override
  public void testA(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(new OperationResult()
      .setStatusCode(200)
      //.setHeaders(MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.CONTENT_TYPE, "application/json"))
      .setPayload(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!").toBuffer())));
  }
}
