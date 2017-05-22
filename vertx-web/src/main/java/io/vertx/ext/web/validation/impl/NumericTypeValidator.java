package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;

import java.util.function.Function;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class NumericTypeValidator<NumberType extends Number> implements ParameterTypeValidator {

  public static Function<String, Integer> parseInteger = (string) -> Integer.parseInt(string);
  public static Function<String, Float> parseFloat = (string) -> Float.parseFloat(string);
  public static Function<String, Double> parseDouble = (string) -> Double.parseDouble(string);
  public static Function<String, Long> parseLong = (string) -> Long.parseLong(string);

  private Function<String, NumberType> parseNumber;
  private Boolean exclusiveMaximum;
  private Double maximum;
  private Boolean exclusiveMinimum;
  private Double minimum;
  private Double multipleOf;

  public NumericTypeValidator(Function<String, NumberType> parseNumber, Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf) {
    this.parseNumber = parseNumber;
    this.exclusiveMaximum = exclusiveMaximum;
    this.maximum = maximum;
    this.exclusiveMinimum = exclusiveMinimum;
    this.minimum = minimum;
    this.multipleOf = multipleOf;
  }

  public NumericTypeValidator(Function<String, NumberType> parseNumber, Double maximum, Double minimum, Double multipleOf) {
    this(parseNumber, false, maximum, false, minimum, multipleOf);
  }

  public NumericTypeValidator(Function<String, NumberType> parseNumber) {
    this(parseNumber, null, null, null, null, null);
  }

  private boolean testMaximum(NumberType number) {
    if (this.maximum != null) {
      if (this.exclusiveMaximum != null && this.exclusiveMaximum)
        return (number.doubleValue() < maximum);
      else
        return (number.doubleValue() <= maximum);
    }
    return true;
  }

  private boolean testMinimum(NumberType number) {
    if (this.minimum != null) {
      if (this.exclusiveMinimum != null && exclusiveMinimum)
        return (number.doubleValue() > minimum);
      else
        return (number.doubleValue() >= minimum);
    }
    return true;
  }

  private boolean testMultipleOf(NumberType number) {
    if (multipleOf != null)
      return (number.doubleValue() % multipleOf == 0);
    else
      return true;
  }

  /**
   * Function that check if parameter is valid
   *
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  @Override
  public boolean isValid(String value) {
    try {
      NumberType number = parseNumber.apply(value);
      if (number != null && this.testMaximum(number) && this.testMinimum(number) && this.testMultipleOf(number)) {
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
