package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;

/**
 * Created by francesco on 19/05/17.
 */
// Need for workaround for 2 or more sub arrays inside query of form
public class Swagger2SubArrayTypeValidator implements ParameterTypeValidator {

  private ParameterTypeValidator validator;
  private String collectionFormat;

  public Swagger2SubArrayTypeValidator(ParameterTypeValidator validator, String collectionFormat) {
    this.validator = validator;
    this.collectionFormat = collectionFormat;
  }

  static String getCollectionSplitter(String collectionFormat) {
    if (collectionFormat != null)
      switch (collectionFormat) {
        case "csv":
          return ",";
        case "ssv":
          return " ";
        case "tsv":
          return "\\";
        case "pipes":
          return "|";
      }
    return ",";
  }

  @Override
  public boolean isValid(String value) {
    for (String s : value.split(Swagger2SubArrayTypeValidator.getCollectionSplitter(this.collectionFormat))) {
      if (!validator.isValid(s))
        return false;
    }
    return true;
  }
}
