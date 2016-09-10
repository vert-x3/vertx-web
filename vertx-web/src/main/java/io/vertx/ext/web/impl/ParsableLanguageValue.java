package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.LanguageHeader;

public class ParsableLanguageValue extends ParsableHeaderValue implements LanguageHeader{

  private String[] parsedValues;
  
  public ParsableLanguageValue(String headerContent) {
    super(headerContent);
    parsedValues = null;
  }

  @Override
  public String tag() {
    return subtag(0);
  }

  @Override
  public @Nullable String subtag() {
    return subtag(1);
  }

  @Override
  public @Nullable String subtag(int level) {
    ensureHeaderProcessed();
    if(level < parsedValues.length){
      return parsedValues[level];
    }
    return null;
  }
  
  @Override
  public int subtagCount(){
    return parsedValues.length;
  }
  
  @Override
  protected void ensureHeaderProcessed() {
    super.ensureHeaderProcessed();
    parsedValues = HeaderParser.parseLanguageValue(value());
  }
  
  protected int weightedOrderPart2() {
    return parsedValues.length;
  }
  
  
}
