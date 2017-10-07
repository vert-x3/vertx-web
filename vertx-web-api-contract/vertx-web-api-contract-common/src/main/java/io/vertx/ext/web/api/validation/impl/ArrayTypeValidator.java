package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ContainerDeserializer;
import io.vertx.ext.web.api.validation.ContainerSerializationStyle;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ArrayTypeValidator extends ContainerTypeValidator<List<String>> {

  private ParameterTypeValidator validator;

  private Integer maxItems;
  private Integer minItems;

  public ArrayTypeValidator(ParameterTypeValidator validator, ContainerDeserializer collectionFormat, boolean
    exploded, Integer maxItems, Integer minItems) {
    super(collectionFormat, exploded);
    // Check illegal inner ArrayTypeValidator
    if (this.validator instanceof ContainerTypeValidator && exploded)
      throw new RuntimeException("Illegal inner type validator");

    this.validator = validator;

    this.maxItems = maxItems;
    this.minItems = minItems;
  }

  private boolean checkMinItems(int size) {
    if (minItems != null) return size >= minItems;
    else return true;
  }

  private boolean checkMaxItems(int size) {
    if (maxItems != null) return size <= maxItems;
    else return true;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    return this.validate(this.deserialize(value));
  }

  @Override
  public RequestParameter isValidCollection(List<String> value) throws ValidationException {
    if (value.size() > 1 && this.isExploded()) {
      return this.validate(value);
    } else {
      return this.validate(this.deserialize(value.get(0)));
    }
  }

  @Override
  protected List<String> deserialize(String serialized) {
    return getContainerDeserializer().deserializeArray(serialized);
  }

  @Override
  protected RequestParameter validate(List<String> values) {
    if (values == null || !checkMaxItems(values.size()) || !checkMinItems(values.size()))
      throw ValidationException.ValidationExceptionFactory.generateUnexpectedArraySizeValidationException(this
        .getMaxItems(), this.getMinItems(), values.size());
    List<RequestParameter> parsedParams = new ArrayList<>();
    for (String s : values) {
      RequestParameter parsed = validator.isValid(s);
      parsedParams.add(parsed);
    }
    return RequestParameter.create(parsedParams);
  }

  public ParameterTypeValidator getInnerValidator() {
    return validator;
  }

  public Integer getMaxItems() {
    return maxItems;
  }

  public Integer getMinItems() {
    return minItems;
  }

  public static class ArrayTypeValidatorFactory {
    public static ArrayTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator) {
      return ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator, "csv", true);
    }

    public static ArrayTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String
      collectionFormat, boolean exploded) {
      return ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator, collectionFormat, exploded,
        null, null);
    }

    public static ArrayTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String
      collectionFormat, Integer maxItems, Integer minItems) {
      return ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator, collectionFormat, true,
        maxItems, minItems);
    }

    public static ArrayTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String
      collectionFormat, boolean exploded, Integer maxItems, Integer minItems) {
      return new ArrayTypeValidator(arrayMembersValidator, ContainerSerializationStyle.getContainerStyle
        (collectionFormat).deserializer(), exploded, maxItems, minItems);
    }
  }
}
