package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterType;
import io.vertx.ext.web.validation.ParameterTypeValidator;

import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ObjectTypeValidator implements ParameterTypeValidator {

  Map<String, ParameterTypeValidator> fieldsMap;


  @Override
  public boolean isValid(String value) {
    //TODO
  }
}
