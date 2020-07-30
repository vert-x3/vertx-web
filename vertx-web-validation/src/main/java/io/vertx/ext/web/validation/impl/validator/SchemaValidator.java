package io.vertx.ext.web.validation.impl.validator;

import io.vertx.core.Future;
import io.vertx.ext.json.schema.NoSyncValidationException;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.ValidationException;
import io.vertx.ext.json.schema.common.SchemaImpl;
import io.vertx.ext.web.validation.RequestParameter;

public class SchemaValidator implements ValueValidator {

  Schema s;

  public SchemaValidator(Schema s) {
    this.s = s;
  }

  @Override
  public Future<RequestParameter> validate(Object json) {
    if (s.isSync()) {
      try {
        s.validateSync(json);
        ((SchemaImpl)s).doApplyDefaultValues(json);
        return Future.succeededFuture(RequestParameter.create(json));
      } catch (ValidationException e) {
        return Future.failedFuture(e);
      }
    }
    return s.validateAsync(json).map(v -> {
      try {
        ((SchemaImpl)s).doApplyDefaultValues(json);
      } catch (NoSyncValidationException e){
        // This happens if I try to apply default values to an async ref schema
      }
      return RequestParameter.create(json);
    });
  }

  @Override
  public Object getDefault() {
    return s.getDefaultValue();
  }

}
