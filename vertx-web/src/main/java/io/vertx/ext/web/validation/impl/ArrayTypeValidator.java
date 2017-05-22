package io.vertx.ext.web.validation.impl;

import io.swagger.models.properties.ArrayProperty;
import io.vertx.ext.web.validation.ParameterTypeValidator;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ArrayTypeValidator implements ParameterTypeValidator {

  public enum CollectionsSplitters {
    csv(","),
    ssv(" "),
    tsv("\\"),
    pipes("|"),
    multi("");

    private String splitter;

    CollectionsSplitters(String splitter) {
      this.splitter = splitter;
    }

    public String getSplitter() {
      return splitter;
    }
  }

  private ParameterTypeValidator validator;
  private CollectionsSplitters collectionFormat;

  private Integer maxItems;
  private Integer minItems;

  public ArrayTypeValidator(ParameterTypeValidator validator, String collectionFormat, Integer maxItems, Integer minItems) {
    this.validator = validator;

    // Check illegal inner ArrayTypeValidator
    if (this.validator instanceof ArrayTypeValidator &&
      ((ArrayTypeValidator) this.validator).getCollectionFormat().equals(CollectionsSplitters.multi))
      throw new RuntimeException("Illegal inner type validator");

    if (collectionFormat != null) {
      CollectionsSplitters splitterEnumValue = CollectionsSplitters.valueOf(collectionFormat);
      if (splitterEnumValue != null)
        this.collectionFormat = splitterEnumValue;
      else
        this.collectionFormat = CollectionsSplitters.multi;
    } else {
      this.collectionFormat = CollectionsSplitters.multi;
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

  static String getCollectionSplitter(CollectionsSplitters collectionFormat) {
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
    for (String s : value.split(ArrayTypeValidator.getCollectionSplitter(this.collectionFormat))) {
      if (!validator.isValid(s))
        return false;
    }
    return true;
  }

  public CollectionsSplitters getCollectionFormat() {
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
