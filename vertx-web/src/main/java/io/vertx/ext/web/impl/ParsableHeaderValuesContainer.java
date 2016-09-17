package io.vertx.ext.web.impl;

import java.util.List;
import java.util.Optional;

import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.ParsedHeaderValue;
import io.vertx.ext.web.ParsedHeaderValues;

public class ParsableHeaderValuesContainer implements ParsedHeaderValues {
  
  private List<MIMEHeader> accept;
  private List<ParsedHeaderValue> acceptCharset;
  private List<ParsedHeaderValue> acceptEncoding;
  private List<LanguageHeader> acceptLanguage;
  private ParsableMIMEValue contentType;
  
  public ParsableHeaderValuesContainer(
      List<MIMEHeader> accept, List<ParsedHeaderValue> acceptCharset, List<ParsedHeaderValue> acceptEncoding,
      List<LanguageHeader> acceptLanguage, ParsableMIMEValue contentType) {
    this.accept = accept;
    this.acceptCharset = acceptCharset;
    this.acceptEncoding = acceptEncoding;
    this.acceptLanguage = acceptLanguage;
    this.contentType = contentType;
  }

  @Override
  public List<MIMEHeader> accept() {
    return accept;
  }
  @Override
  public List<ParsedHeaderValue> acceptCharset() {
    return acceptCharset;
  }
  @Override
  public List<ParsedHeaderValue> acceptEncoding() {
    return acceptEncoding;
  }
  @Override
  public List<LanguageHeader> acceptLanguage() {
    return acceptLanguage;
  }
  @Override
  public ParsableMIMEValue contentType() {
    return contentType;
  }
  
  @Override
  public <T extends ParsedHeaderValue> Optional<T> findBestUserAcceptedIn(List<T> userAccepted, Iterable<T> in) {
    for (T acceptableType: userAccepted) {
      Optional<T> acceptedType = acceptableType.findMatchedBy(in);
      if(acceptedType.isPresent()){
        return acceptedType;
      }
    }
    return Optional.empty();
  }
}
