package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class BooleanTypeValidatorTest {

  @Test
  public void isValidTrue() {
    BooleanTypeValidator validator = new BooleanTypeValidator(false);
    assertEquals(RequestParameter.create(true), validator.isValid("true"));
    assertEquals(RequestParameter.create(true), validator.isValid("t"));
    assertEquals(RequestParameter.create(true), validator.isValid("1"));
  }

  @Test
  public void isValidFalse() {
    BooleanTypeValidator validator = new BooleanTypeValidator(true);
    assertEquals(RequestParameter.create(false), validator.isValid("false"));
    assertEquals(RequestParameter.create(false), validator.isValid("f"));
    assertEquals(RequestParameter.create(false), validator.isValid("0"));
  }

  @Test(expected = ValidationException.class)
  public void isNotValid() {
    BooleanTypeValidator validator = new BooleanTypeValidator(false);
    validator.isValid("2");
  }

  @Test
  public void isValidDefault() {
    BooleanTypeValidator validator = new BooleanTypeValidator(true);
    assertEquals(RequestParameter.create(true), validator.isValid(null));
  }

  @Test
  public void isValidCollection() {
    BooleanTypeValidator validator = new BooleanTypeValidator(null);
    assertEquals(RequestParameter.create(true), validator.isValidCollection(Arrays.asList("true")));
  }

  @Test(expected = ValidationException.class)
  public void isValidCollectionFailure() {
    BooleanTypeValidator validator = new BooleanTypeValidator(null);
    validator.isValidCollection(Arrays.asList("true", "false", "true"));
  }

}
