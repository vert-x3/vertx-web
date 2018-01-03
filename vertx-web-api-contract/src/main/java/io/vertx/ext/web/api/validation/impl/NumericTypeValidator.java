package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.function.Function;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class NumericTypeValidator implements ParameterTypeValidator {

  private static final Function<String, Number> parseInteger = Integer::valueOf;
  private static final Function<String, Number> parseFloat = Float::parseFloat;
  private static final Function<String, Number> parseDouble = Double::parseDouble;
  private static final Function<String, Number> parseLong = Long::parseLong;

  private Function<String, Number> parseNumber;
  private Boolean exclusiveMaximum;
  private Double maximum;
  private Boolean exclusiveMinimum;
  private Double minimum;
  private Double multipleOf;
  private Number defaultValue;

  public NumericTypeValidator(Class numberType,
                              Boolean exclusiveMaximum, Double maximum,
                              Boolean exclusiveMinimum, Double minimum,
                              Double multipleOf, Object defaultValue) {
    if (Integer.class.equals(numberType))
      this.parseNumber = parseInteger;
    else if (Float.class.equals(numberType))
      this.parseNumber = parseFloat;
    else if (Double.class.equals(numberType))
      this.parseNumber = parseDouble;
    else if (Long.class.equals(numberType))
      this.parseNumber = parseLong;
    else
      throw new IllegalArgumentException("numberType can be Integer.class, Float.class, Double.class or Long.class");

    this.exclusiveMaximum = exclusiveMaximum;
    this.maximum = maximum;
    this.exclusiveMinimum = exclusiveMinimum;
    this.minimum = minimum;
    this.multipleOf = multipleOf;
    if (defaultValue != null) {
      if (defaultValue instanceof String)
        this.defaultValue = parseNumber.apply((String) defaultValue);
      else if (numberType.equals(defaultValue.getClass())) {
        this.defaultValue = (Number) numberType.cast(defaultValue);
      } else {
        throw new IllegalArgumentException("defaultValue should be a String or a Number instance");
      }
    }
  }

  public NumericTypeValidator(Class numberType, Double maximum, Double minimum, Double multipleOf, Object defaultValue) {
    this(numberType, false, maximum, false, minimum, multipleOf, defaultValue);
  }

  public NumericTypeValidator(Class numberType, Object defaultValue) {
    this(numberType, null, null, null, null, null, defaultValue);
  }

  public NumericTypeValidator(Class numberType) {
    this(numberType, null, null, null, null, null, null);
  }

  private boolean testMaximum(Number number) {
    if (this.maximum != null) {
      if (this.exclusiveMaximum != null && this.exclusiveMaximum) return (number.doubleValue() < maximum);
      else return (number.doubleValue() <= maximum);
    }
    return true;
  }

  private boolean testMinimum(Number number) {
    if (this.minimum != null) {
      if (this.exclusiveMinimum != null && exclusiveMinimum) return (number.doubleValue() > minimum);
      else return (number.doubleValue() >= minimum);
    }
    return true;
  }

  private boolean testMultipleOf(Number number) {
    if (multipleOf != null) return (number.doubleValue() % multipleOf == 0);
    else return true;
  }

  /**
   * Function that check if parameter is valid
   *
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  @Override
  public RequestParameter isValid(String value) {
    if (value == null) return RequestParameter.create(getDefault());
    try {
      Number number = parseNumber.apply(value);
      if (number != null && this.testMaximum(number) && this.testMinimum(number) && this.testMultipleOf(number)) {
        return RequestParameter.create(number);
      } else {
        throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException("Invalid number");
      }
    } catch (NumberFormatException e) {
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException("Value is not a valid number");
    }
  }

  @Override
  public Object getDefault() {
    return defaultValue;
  }
}
