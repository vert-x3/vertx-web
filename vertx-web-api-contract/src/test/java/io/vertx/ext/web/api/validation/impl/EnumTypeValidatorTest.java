package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class EnumTypeValidatorTest {

  @Test
  public void isValid() {
    List<String> strings = Arrays.asList("hello", "world", "im", "francesco", "and", "i", "dont", "like", "testing");
    EnumTypeValidator enumTypeValidator = new EnumTypeValidator<>(strings, null);
    strings.forEach((s) -> assertEquals(RequestParameter.create(s), enumTypeValidator.isValid(s)));
  }

  @Test
  public void isValidEnumIntegers() {
    List<String> strings = Arrays.asList("1", "2", "3", "4", "5");
    EnumTypeValidator enumTypeValidator = new EnumTypeValidator<>(strings.stream().map(Integer::valueOf).collect(Collectors.toList()), new NumericTypeValidator(Integer.class));
    strings.forEach((s) -> assertEquals(RequestParameter.create(Integer.valueOf(s)), enumTypeValidator.isValid(s)));
  }

  @Test(expected = ValidationException.class)
  public void isNotValid() {
    EnumTypeValidator enumTypeValidator = new EnumTypeValidator<>(Arrays.asList("hello", "world"), null);
    enumTypeValidator.isValid("francesco");
  }
}
