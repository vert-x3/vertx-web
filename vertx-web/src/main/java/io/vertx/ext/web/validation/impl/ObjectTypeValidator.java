package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.*;

import java.lang.reflect.Parameter;
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
  public void isValid(String value) throws ValidationException {
    this.validate(this.deserialize(value));
  }

  @Override
  public void isValidCollection(List<String> value) throws ValidationException {
    this.validate(this.deserialize(value.get(0)));
  }

  @Override
  protected Map<String, String> deserialize(String serialized) throws ValidationException {
    return getContainerDeserializer().deserializeObject(serialized);
  }

  @Override
  protected void validate(Map<String, String> values) throws ValidationException {
    for (Map.Entry<String, ObjectField> field : fieldsMap.entrySet()) {
      String valueToValidate = values.get(field.getKey());
      if (valueToValidate == null) {
        if (field.getValue().required)
          throw ValidationException.generateObjectFieldNotFound(field.getKey());
      } else {
        field.getValue().validator.isValid(valueToValidate);
      }
    }
  }

  public static class ObjectTypeValidatorFactory {
    public static ObjectTypeValidator createObjectTypeValidator(ContainerSerializationStyle collectionFormat, boolean exploded) {
      return new ObjectTypeValidator(collectionFormat.getDeserializer(), exploded);
    }

    public static ObjectTypeValidator createObjectTypeValidator(String collectionFormat, boolean exploded) {
      return new ObjectTypeValidator(ContainerSerializationStyle.getContainerStyle(collectionFormat).getDeserializer(), exploded);
    }
  }

}
