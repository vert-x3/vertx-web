package io.vertx.ext.web.validation.impl.body;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.validator.ValueValidator;

public class JsonBodyProcessorImpl implements BodyProcessor {

  private ValueValidator valueValidator;

  public JsonBodyProcessorImpl(ValueValidator valueValidator) {
    this.valueValidator = valueValidator;
  }

  @Override
  public boolean canProcess(String contentType) {
    return Utils.isJsonContentType(contentType);
  }

  @Override
  public Future<RequestParameter> process(RoutingContext requestContext) {
    try {
      Buffer body = requestContext.body().buffer();
      if (body == null) {
        throw BodyProcessorException.createParsingError(
          requestContext.request().getHeader(HttpHeaders.CONTENT_TYPE),
          new MalformedValueException("Null body")
        );
      }
      Object json = Json.decodeValue(body);
      return valueValidator.validate(json).recover(err -> Future.failedFuture(
        BodyProcessorException.createValidationError(requestContext.request().getHeader(HttpHeaders.CONTENT_TYPE), err)
      ));
    } catch (DecodeException e) {
      throw BodyProcessorException.createParsingError(requestContext.request().getHeader(HttpHeaders.CONTENT_TYPE), e);
    }
  }
}
