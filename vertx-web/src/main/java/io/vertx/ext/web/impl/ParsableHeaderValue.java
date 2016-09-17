package io.vertx.ext.web.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import io.vertx.ext.web.ParsedHeaderValue;

public class ParsableHeaderValue implements ParsedHeaderValue {
  
  private String headerContent;
  
  private String value;
  private float weight;

  private Map<String, String> parameter;
  
  private int paramsWeight;
  
  
  public ParsableHeaderValue(String headerContent) {
    Objects.requireNonNull(headerContent, "headerContent must not be null");
    this.headerContent = headerContent;
    value = null;
    weight = -1;
    parameter = Collections.emptyMap();
  }
  
  @Override
  public String rawValue() {
    return headerContent;
  }
  
  @Override
  public String value() {
    return value;
  }
  
  @Override
  public float weight() {
    ensureHeaderProcessed();
    return weight;
  }

  @Override
  public boolean isPermitted() {
    ensureHeaderProcessed();
    // rfc7231 states the least possible number above 0 is 0.001
    return weight < 0.001;
  }
  
  public String getParameter(String key) {
    return parameter.get(key);
  }
  
  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(parameter);
  }
  
  public final boolean isMatchedBy(ParsableHeaderValue matchTry){
    return this.headerContent.equals(matchTry.headerContent) || isMatchedBy2(matchTry);
  }
  
  protected boolean isMatchedBy2(ParsableHeaderValue matchTry){
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
  
  @Override
  public <T extends ParsedHeaderValue> Optional<T> findMatchedBy(Iterable<T> matchTries) {
    
    for (T matchTry : matchTries) {
      if(isMatchedBy((ParsableHeaderValue) matchTry)){
        return Optional.of(matchTry);
      }
    }
    return Optional.empty();
  }
  
  private void ensureParameterIsHashMap() {
    if(parameter.isEmpty()){
      parameter = new HashMap<>();
    }
  }

  protected void ensureHeaderProcessed() {
    if(weight < 0){
      // as per rfc7231, the default value is 1
      weight = DEFAULT_WEIGHT;
      HeaderParser.parseHeaderValue(
          headerContent,
          this::setValue,
          this::setWeight,
          this::addParameter
      );
      
      paramsWeight = parameter.isEmpty() ? 0 : 1;
    }
  }
  
  public ParsableHeaderValue forceParse(){
    ensureHeaderProcessed();
    return this;
  }
  
  private void setValue(String value) {
    this.value = value;
  }
  private void addParameter(String key, String value) {
    ensureParameterIsHashMap();
    if(value == null){
      value = EMPTY;
      paramsWeight = Math.max(1, paramsWeight);
    } else {
      paramsWeight = Math.max(2, paramsWeight);
    }
    parameter.put(key, value);
  }
  private void setWeight(float weight) {
    // Keep between 0 and 1 while dropping after the 3rd digit to the right (rfc7231#section-5.3.1)
    this.weight = ((int)((Math.max(0, Math.min(1, weight)) * 100)) / 100.0f);
  }
  
  public final int weightedOrder(){
    ensureHeaderProcessed();
    return (int)(weight() * 1000) + (weightedOrderPart2() * 10) + paramsWeight;
  }
  
  protected int weightedOrderPart2(){
    return 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(headerContent);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof ParsableHeaderValue)) {
      return false;
    }
    ParsableHeaderValue other = (ParsableHeaderValue) obj;
    if (headerContent == null) {
      if (other.headerContent != null) {
        return false;
      }
    } else if (!headerContent.equals(other.headerContent)) {
      return false;
    }
    return true;
  }
  
}
