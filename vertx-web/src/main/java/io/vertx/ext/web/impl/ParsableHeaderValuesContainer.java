package io.vertx.ext.web.impl;

import java.util.Collection;
import java.util.List;

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
  public <T extends ParsedHeaderValue> T findBestUserAcceptedIn(List<T> userAccepted, Collection<T> in) {
    for (T acceptableType: userAccepted) {
      T acceptedType = acceptableType.findMatchedBy(in);
      if(acceptedType != null){
        return acceptedType;
      }
    }
    return null;
  }
}
