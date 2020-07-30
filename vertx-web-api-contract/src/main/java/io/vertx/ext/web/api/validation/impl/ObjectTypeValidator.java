package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ContainerDeserializer;
import io.vertx.ext.web.api.validation.ContainerSerializationStyle;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ObjectTypeValidator extends ContainerTypeValidator<Map<String, String>> {

  private class ObjectField {
    private ParameterTypeValidator validator;
    private boolean required;

    public ObjectField(ParameterTypeValidator validator, boolean required) {
      this.validator = validator;
      this.required = required;
    }
  }

  Map<String, ObjectField> fieldsMap;

  public ObjectTypeValidator(ContainerDeserializer collectionFormat, boolean exploded) {
    super(collectionFormat, exploded);
    this.fieldsMap = new HashMap<>();
  }

  public void addField(String name, ParameterTypeValidator validator, boolean required) {
    fieldsMap.put(name, new ObjectField(validator, required));
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    return this.validate(this.deserialize(value));
  }

  @Override
  public RequestParameter isValidCollection(List<String> value) throws ValidationException {
    return this.validate(this.deserialize(value.get(0)));
  }

  @Override
  protected Map<String, String> deserialize(String serialized) throws ValidationException {
    return getContainerDeserializer().deserializeObject(serialized);
  }

  @Override
  protected RequestParameter validate(Map<String, String> values) throws ValidationException {
    Map<String, RequestParameter> parsedParams = new HashMap<>();

    for (Map.Entry<String, ObjectField> field : fieldsMap.entrySet()) {
      if (!values.containsKey(field.getKey())) {
        if (field.getValue().required)
          throw ValidationException.ValidationExceptionFactory.generateObjectFieldNotFound(field.getKey());
        else if (field.getValue().validator.getDefault() != null)
          parsedParams.put(field.getKey(), RequestParameter.create(field.getKey(), field.getValue().validator
            .getDefault()));

      } else {
        RequestParameter param = field.getValue().validator.isValid(values.get(field.getKey()));
        param.setName(field.getKey());
        parsedParams.put(field.getKey(), param);
      }
    }

    return RequestParameter.create(parsedParams);
  }

  public static class ObjectTypeValidatorFactory {
    public static ObjectTypeValidator createObjectTypeValidator(ContainerSerializationStyle collectionFormat, boolean
      exploded) {
      return new ObjectTypeValidator(collectionFormat.deserializer(), exploded);
    }

    public static ObjectTypeValidator createObjectTypeValidator(String collectionFormat, boolean exploded) {
      return new ObjectTypeValidator(ContainerSerializationStyle.getContainerStyle(collectionFormat).deserializer(),
        exploded);
    }
  }

}
