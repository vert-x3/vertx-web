package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Locale;

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
  public String language() {
    String value =  tag();
    return value == null ? null : value.toLowerCase();
  }

  @Override
  public @Nullable String subtag() {
    return subtag(1);
  }

  @Override
  public @Nullable String country() {
    String value = subtag(1);
    return value == null ? null : value.toUpperCase();
  }

  @Override
  public @Nullable String variant() {
    String value = subtag(2);
    return value == null ? null : value.toUpperCase();
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
  protected boolean isMatchedBy2(ParsableHeaderValue matchTry) {
    ParsableLanguageValue myMatchTry = (ParsableLanguageValue) matchTry;
    ensureHeaderProcessed();
    
    for (int i = 0; i < myMatchTry.parsedValues.length; i++) {
      String match = myMatchTry.parsedValues[i];
      String against = this.parsedValues[i];
      if(match != STAR && !match.equalsIgnoreCase(against)){
        return false;
      }
    }
    return super.isMatchedBy2(myMatchTry);
  }
  
  @Override
  protected void ensureHeaderProcessed() {
    super.ensureHeaderProcessed();
    parsedValues = HeaderParser.parseLanguageValue(value);
  }
    
}
