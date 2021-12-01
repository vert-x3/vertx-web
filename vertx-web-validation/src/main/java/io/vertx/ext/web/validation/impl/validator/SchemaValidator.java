package io.vertx.ext.web.validation.impl.validator;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.json.schema.NoSyncValidationException;
import io.vertx.json.schema.Schema;
import io.vertx.json.schema.ValidationException;
import io.vertx.json.schema.common.SchemaImpl;
import io.vertx.json.schema.validator.OutputUnit;
import io.vertx.json.schema.validator.Validator;

public class SchemaValidator implements ValueValidator {

  private final Schema s;
  private final Validator validator;

  @Deprecated
  public SchemaValidator(Schema s) {
    this.s = s;
    this.validator = null;
  }

  public SchemaValidator(Validator validator) {
    this.validator = validator;
    this.s = null;
  }

  @Override
  public Future<RequestParameter> validate(Object json) {
    if (validator != null) {
      try {
        OutputUnit res = validator.validate(json);
        if (res.getValid()) {
          return Future.succeededFuture(RequestParameter.create(json));
        } else {
          // when the validation fails, there are a list of errors and annotations
          // while annotations are non fatal we should use the list of errors to describe the failure?
          return Future.failedFuture(res.getError());
        }
      } catch (IllegalStateException e) {
        // illegal state exception is thrown when the resolved schemas reach an illegal point
        return Future.failedFuture(e);
      }
    }
    if (s.isSync()) {
      try {
        s.validateSync(json);
        ((SchemaImpl) s).getOrApplyDefaultSync(json);
        return Future.succeededFuture(RequestParameter.create(json));
      } catch (ValidationException e) {
        return Future.failedFuture(e);
      }
    }
    return s.validateAsync(json).map(v -> {
      try {
        ((SchemaImpl) s).getOrApplyDefaultAsync(json);
      } catch (NoSyncValidationException e) {
        // This happens if I try to apply default values to an async ref schema
      }
      return RequestParameter.create(json);
    });
  }

  @Override
  public Future<Object> getDefault() {
    if (s.isSync()) {
      return Future.succeededFuture( ((SchemaImpl) s).getOrApplyDefaultSync(null));
    }
    return ((SchemaImpl) s).getOrApplyDefaultAsync(null);
  }
}
