package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ContainerSerializationStyle;
import io.vertx.ext.web.validation.ParameterTypeValidator;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ArrayTypeValidator extends ContainerTypeValidator<List<String>> {

  private ParameterTypeValidator validator;

  private Integer maxItems;
  private Integer minItems;

  public ArrayTypeValidator(ParameterTypeValidator validator, String collectionFormat, Integer maxItems, Integer minItems) {
    this.validator = validator;

    // Check illegal inner ArrayTypeValidator
    if (this.validator instanceof ArrayTypeValidator &&
      ((ArrayTypeValidator) this.validator).getCollectionFormat().equals(ContainerSerializationStyle.explode))
      throw new RuntimeException("Illegal inner type validator");

    if (collectionFormat != null) {
      ContainerSerializationStyle splitterEnumValue = ContainerSerializationStyle.valueOf(collectionFormat);
      if (splitterEnumValue != null)
        this.collectionFormat = splitterEnumValue;
      else
        this.collectionFormat = ContainerSerializationStyle.explode;
    } else {
      this.collectionFormat = ContainerSerializationStyle.explode;
    }
    this.maxItems = maxItems;
    this.minItems = minItems;
  }

  public ArrayTypeValidator(ParameterTypeValidator validator, String collectionFormat) {
    this(validator, collectionFormat, null, null);
  }

  public ArrayTypeValidator(ParameterTypeValidator validator) {
    this(validator, null);
  }

  static String getCollectionSplitter(ContainerSerializationStyle collectionFormat) {
    if (collectionFormat != null) {
      return collectionFormat.getSplitter();
    }
    return ",";
  }

  private boolean checkMinItems(int size) {
    if (minItems != null)
      return size >= minItems;
    else return true;
  }

  private boolean checkMaxItems(int size) {
    if (maxItems != null)
      return size <= maxItems;
    else return true;
  }

  @Override
  public boolean isValid(String value) {
    for (String s : this.deserialize(value)) {
      if (!validator.isValid(s))
        return false;
    }
    return true;
  }

  public ContainerSerializationStyle getCollectionFormat() {
    return collectionFormat;
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
}
