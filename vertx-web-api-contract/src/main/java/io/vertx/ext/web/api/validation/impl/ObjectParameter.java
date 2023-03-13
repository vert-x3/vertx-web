package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.validation.ParameterValidationRule;

import java.util.HashMap;
import java.util.Map;

public class ObjectParameter {
  private String name;
  private boolean isOptional;
  private Map<String, ParameterValidationRule> paramsRules;

  public ObjectParameter(String name, boolean isOptional) {
    this.name = name;
    this.isOptional = isOptional;
    this.paramsRules = new HashMap<>();
  }

  public String getName() {
    return name;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public void addParameterValidationRule(ParameterValidationRule rule) {
    if (!paramsRules.containsKey(rule.getName())) paramsRules.put(rule.getName(), rule);
  }

  public Map<String, ParameterValidationRule> getParamsRules() {
    return paramsRules;
  }
}
