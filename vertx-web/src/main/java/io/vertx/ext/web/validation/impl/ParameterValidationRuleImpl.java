package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ParameterValidationRuleImpl implements ParameterValidationRule {

  private String name;
  ParameterTypeValidator validator;
  private ParameterLocation location;

  private boolean isOptional;
  private boolean allowEmptyValue;

  // Array params, this params are used ONLY IF array collectionformat is explode, otherwise it will be handled by the validator
  private boolean explodedCollection; // If parameter is a explode array, this object have to manually loop inside it, otherwise, call validator function
  private ContainerSerializationStyle explodedCollectionStyle;

  public ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, boolean allowEmptyValue, ParameterLocation location) {
    if (name == null)
      throw new NullPointerException("name cannot be null");
    this.name = name;
    if (validator == null)
      throw new NullPointerException("validator cannot be null");
    this.validator = validator;
    this.isOptional = isOptional;
    this.allowEmptyValue = allowEmptyValue;
    this.location = location;

    // Multi array construction routine
    if (validator instanceof ContainerTypeValidator && !location.equals(ParameterLocation.BODY_FORM)) {
      ContainerTypeValidator arrayValidator = (ContainerTypeValidator) validator;
      this.explodedCollection = arrayValidator.isExploded();
      this.explodedCollectionStyle = arrayValidator.getCollectionFormat();
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  private void callValidator(String value) throws ValidationException {
    if (!validator.isValid(value))
      throw ValidationException.generateNotMatchValidationException(this.name, value, this, this.location);
  }

  //TODO move
  private boolean checkMinItems(int size) {
    if (minItems != null)
      return size >= minItems;
    else return true;
  }

  //TODO move
  private boolean checkMaxItems(int size) {
    if (maxItems != null)
      return size <= maxItems;
    else return true;
  }

  private String serializeExpandedRFC6570FormStyleExpansionQueryParameter(List<String> list) throws UnsupportedEncodingException {
    StringBuilder stringBuilder = new StringBuilder();
    for (String v : list) {
      stringBuilder.append(URLEncoder.encode(v, "UTF-8") + ",");
    }
    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length() - 1);
    return stringBuilder.toString();
  }

  @Override
  public void validateSingleParam(String value) throws ValidationException {
    if (value != null && value.length() != 0) {
      callValidator(value);
    } else {
      // Value or null or length == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
    }
  }

  @Override
  public void validateArrayParam(List<String> value) throws ValidationException {
    if (value != null && value.size() != 0) {
      if (this.explodedCollection) {
        if (explodedCollectionStyle.equals(ContainerSerializationStyle.rfc6570_form_style_query_parameter_expansion) && location.equals(ParameterLocation.QUERY)) {
          String serializedValue = null;
          try {
            serializedValue = this.serializeExpandedRFC6570FormStyleExpansionQueryParameter(value);
          } catch (UnsupportedEncodingException e) {
            //TODO throw ValidationException
          }
          validateSingleParam(serializedValue);
        }
      } else {
        validateSingleParam(value.get(0));
      }
    } else {
      // array or null or size == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
    }
  }

  @Override
  public boolean isOptional() {
    return isOptional;
  }

  @Override
  public ParameterTypeValidator getParameterTypeValidator() {
    return validator;
  }

  @Override
  public boolean isExplodedCollection() {
    return explodedCollection;
  }

  @Override
  public String toString() {
    return "ParameterValidationRuleImpl{" +
      "name='" + name + '\'' +
      ", validator=" + validator +
      ", location=" + location +
      ", isOptional=" + isOptional +
      ", allowEmptyValue=" + allowEmptyValue +
      ", explodedCollection=" + explodedCollection +
      '}';
  }
}
