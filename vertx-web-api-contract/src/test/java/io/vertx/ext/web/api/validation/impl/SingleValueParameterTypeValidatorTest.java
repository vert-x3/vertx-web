package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class SingleValueParameterTypeValidatorTest {

  private class MockSingleValueParameterTypeValidator extends SingleValueParameterTypeValidator<String> {

    public MockSingleValueParameterTypeValidator(String defaultValue) {
      super(defaultValue);
    }

    @Override
    public RequestParameter isValidSingleParam(String value) {
      return RequestParameter.create(value);
    }
  }

  @Test
  public void defaultTest() {
    ParameterTypeValidator validator = new MockSingleValueParameterTypeValidator("hello");
    assertEquals(RequestParameter.create("hello"), validator.isValid(null));
    assertEquals(RequestParameter.create("world"), validator.isValid("world"));
  }

  @Test(expected = ValidationException.class)
  public void nullTest() {
    ParameterTypeValidator validator = new MockSingleValueParameterTypeValidator(null);
    validator.isValid(null);
  }

}
