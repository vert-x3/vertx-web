package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import java.util.UUID;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class StringTypeValidatorTest {

  @Test
  public void isValidPattern() {
    ParameterTypeValidator validator = new StringTypeValidator(RegularExpressions.EMAIL);
    validator.isValid("admin@vertx.io");
  }

  @Test(expected = ValidationException.class)
  public void isNotValidPattern() {
    ParameterTypeValidator validator = new StringTypeValidator(RegularExpressions.EMAIL);
    validator.isValid("admin.vertx.io");
  }

  @Test
  public void isValidMaxLength() {
    ParameterTypeValidator validator = new StringTypeValidator(null, null, 3, null);
    validator.isValid("aaa");
  }

  @Test(expected = ValidationException.class)
  public void isNotValidMaxLength() {
    ParameterTypeValidator validator = new StringTypeValidator(null, null, 3, null);
    validator.isValid("aaaa");
  }

  @Test
  public void isValidMinLength() {
    ParameterTypeValidator validator = new StringTypeValidator(null, 2, null, null);
    validator.isValid("aaa");
  }

  @Test(expected = ValidationException.class)
  public void isNotValidMinLength() {
    ParameterTypeValidator validator = new StringTypeValidator(null, 2, null, null);
    validator.isValid("a");
  }

  @Test
  public void isValidUUID() {
    ParameterTypeValidator validator = new StringTypeValidator(RegularExpressions.UUID);
    validator.isValid(UUID.randomUUID().toString());
  }

  @Test(expected = ValidationException.class)
  public void isNotValidUUID() {
    ParameterTypeValidator validator = new StringTypeValidator(RegularExpressions.UUID);
    validator.isValid(UUID.randomUUID().toString().replaceAll("-","#"));
  }
}
