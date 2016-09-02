package io.vertx.ext.web.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import io.vertx.codegen.annotations.Fluent;

public class ParsedMIME implements Comparable<ParsedMIME>{
  
  private String component;
  private String subComponent;
  
  private Map<String, String> parameter;

  private static final String STAR = "*";
  private static final String EMPTY = "";
  
  public ParsedMIME(String mimeComponent, String mimeSubComponent) {
    
    this.component = mimeComponent.equals(STAR) ? STAR : mimeComponent;
    this.subComponent = mimeSubComponent.equals(STAR) ? STAR : mimeSubComponent;
  }
  
  /**
   * Import a ParsedMIME
   */
  protected ParsedMIME(ParsedMIME mime) {
    this(mime.component, mime.subComponent);
    parameter = mime.parameter;
  }

  @Fluent
  public ParsedMIME addParameter(String key, String value) {
    ensureParameterExists();
    
    parameter.put(key, value);
    return this;
  }
  
  @Fluent
  public ParsedMIME addParameter(String key) {
    ensureParameterExists();
    
    parameter.put(key, EMPTY);
    return this;
  }
  
  /**
   * Tests if this MIME complies with matchTry MIME
   * @return
   */
  public boolean isMatchedBy(ParsedMIME matchTry) {

    if (component != STAR && !component.equals(matchTry.component)) {
      return false;
    }
    if (subComponent != STAR && !subComponent.equals(matchTry.subComponent)) {
      return false;
    }
    
    if (matchTry.parameter == null) {
      return true;
      
    }
    if (parameter == null) {
      return false;
    }
      
    for (Entry<String, String> requiredParameter : matchTry.parameter.entrySet()) {
      String parameterValueToTest = parameter.get(requiredParameter.getKey());
      String requiredParamVal = requiredParameter.getValue();
      
      if (parameterValueToTest == null || (
          requiredParamVal != null && !requiredParamVal.equals(parameterValueToTest))
         ){
        return false;
      }
    }
    return true;
  }
  
  public Optional<ParsedMIME> findMatchedBy(Iterable<? extends ParsedMIME> matchTries) {
    
    for (ParsedMIME matchTry : matchTries) {
      if(isMatchedBy(matchTry)){
        return Optional.of(matchTry);
      }
    }
    return Optional.empty();
  }
  
  private void ensureParameterExists() {
    if(parameter == null){
      parameter = new HashMap<>();
    }
  }

  private int compareParam1ToParam2(String param1, String param2){
    int param1IsStar = param1.equals("*") ? 0 : 1;
    int param2IsStar = param2.equals("*") ? 0 : 1;
    
    return param2IsStar - param1IsStar;
  }
  
  @Override
  /**
   * This still has a long way to optimization. As long as reducing each class to 1 number, if needed
   */
  public int compareTo(ParsedMIME other) {
    // The most specific is always the most relevant
    
    int componentComparison = compareParam1ToParam2(this.component, other.component);
    if(componentComparison != 0){
      return componentComparison;
    }
    
    int subComponentComparison = compareParam1ToParam2(this.subComponent, other.subComponent);
    if(subComponentComparison != 0){
      return subComponentComparison;
    }
    
    if(this.parameter == null && other.parameter == null){
      return 0;
    }
    
    return this.parameter == null ? 1 : -1;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(component, parameter, subComponent);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ParsedMIME other = (ParsedMIME) obj;
    return  Objects.equals(this.component, other.component) &&
            Objects.equals(this.subComponent, other.subComponent) &&
            Objects.equals(this.parameter, other.parameter);
  }

  @Override
  public String toString() {
    return "ParsedMIME [component=" + component + ", subComponent=" + subComponent + ", parameter=" + parameter + "]";
  }
  
}
