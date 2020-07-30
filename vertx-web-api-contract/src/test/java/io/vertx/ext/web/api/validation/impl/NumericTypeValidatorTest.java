package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class NumericTypeValidatorTest {

  @Test
  public void constructorInteger() {
    new NumericTypeValidator(Integer.class, 1);
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, "1");
    assertEquals(1, v.getDefault());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorIntegerFailure() {
    new NumericTypeValidator(Integer.class, 2.4);
  }

  @Test
  public void constructorLong() {
    new NumericTypeValidator(Long.class, 1L);
    NumericTypeValidator v = new NumericTypeValidator(Long.class, "1");
    assertEquals(1L, v.getDefault());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorLongFailure() {
    new NumericTypeValidator(Long.class, 1f);
  }

  @Test
  public void constructorDouble() {
    new NumericTypeValidator(Double.class, 1d);
    NumericTypeValidator v = new NumericTypeValidator(Double.class, "1");
    assertEquals(1d, v.getDefault());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorDoubleFailure() {
    new NumericTypeValidator(Double.class, 1f);
  }

  @Test
  public void constructorFloat() {
    new NumericTypeValidator(Float.class, 1f);
    NumericTypeValidator v = new NumericTypeValidator(Float.class, "1");
    assertEquals(1f, v.getDefault());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorFloatFailure() {
    new NumericTypeValidator(Float.class, 1d);
  }

  @Test
  public void isValid() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class);
    v.isValid("1");
  }

  @Test
  public void isValidDefault() {
    Integer i = 1;
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, i);
    assertEquals(i, v.isValid(null).getInteger());
  }

  @Test
  public void isValidMaximum() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, false, 10d, null, null, null, null);
    v.isValid("10");
    v.isValid("9");
  }

  @Test
  public void isValidExclusiveMaximum() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, true, 10d, null, null, null, null);
    v.isValid("9");
  }

  @Test(expected = ValidationException.class)
  public void isValidMaximumFailure() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, false, 10d, null, null, null, null);
    v.isValid("11");
  }

  @Test(expected = ValidationException.class)
  public void isValidExclusiveMaximumFailure() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, true, 10d, null, null, null, null);
    v.isValid("10");
  }

  @Test
  public void isValidMinimum() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, false, -10d, null, null);
    v.isValid("-10");
    v.isValid("-9");
  }

  @Test
  public void isValidExclusiveMinimum() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, true, -10d, null, null);
    v.isValid("-9");
  }

  @Test(expected = ValidationException.class)
  public void isValidMinimumFailure() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, false, -10d, null, null);
    v.isValid("-11");
  }

  @Test(expected = ValidationException.class)
  public void isValidExclusiveMinimumFailure() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, true, -10d, null, null);
    v.isValid("-10");
  }

  @Test
  public void isValidMultipleOf() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, null, null, 2d, null);
    v.isValid("4");
  }

  @Test(expected = ValidationException.class)
  public void isValidMultipleOfFailure() {
    NumericTypeValidator v = new NumericTypeValidator(Integer.class, null, null, null, null, 2d, null);
    v.isValid("3");
  }

  @Test(expected = ValidationException.class)
  public void isValidEmptyStringFailure() {
    NumericTypeValidator validator = new NumericTypeValidator(Integer.class);
    validator.isValid("");
  }

  @Test(expected = ValidationException.class)
  public void isValidNullStringFailure() {
    NumericTypeValidator validator = new NumericTypeValidator(Integer.class);
    validator.isValid("");
  }

  @Test
  public void isValidCollection() {
    NumericTypeValidator validator = new NumericTypeValidator(Integer.class);
    validator.isValidCollection(Arrays.asList("1"));
  }

  @Test(expected = ValidationException.class)
  public void isValidCollectionFailure() {
    NumericTypeValidator validator = new NumericTypeValidator(Integer.class);
    validator.isValidCollection(Arrays.asList("1", "2", "3"));
  }
}
