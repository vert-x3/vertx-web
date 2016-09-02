package io.vertx.ext.web.impl;

public class UserParsedMIME extends ParsedMIME {
  
  private String originalMIME;
  
  public UserParsedMIME(ParsedMIME mime, String originalMIME) {
    super(mime);
    this.originalMIME = originalMIME;
  }
  
  public String original(){
    return originalMIME;
  }
  
}
