package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.ParsedHeaderValue;
import io.vertx.ext.web.ParsedHeaderValues;

import java.util.Collection;
import java.util.List;

public class ParsableHeaderValuesContainer implements ParsedHeaderValues {

  private final List<MIMEHeader> accept;
  private final List<ParsedHeaderValue> acceptCharset;
  private final List<ParsedHeaderValue> acceptEncoding;
  private final List<LanguageHeader> acceptLanguage;
  private final ParsableMIMEValue contentType;

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
  public @Nullable MIMEHeader findBestUserAcceptedIn(List<MIMEHeader> userAccepted, Collection<MIMEHeader> in) {
    for (MIMEHeader acceptableType: userAccepted) {
      MIMEHeader acceptedType = acceptableType.findMatchedBy(in);
      if (acceptedType != null) {
        if ("*".equals(acceptedType.subComponent()) || "*".equals(acceptedType.component()))
          return acceptableType;
        else
          return acceptedType;
      }
    }
    return null;
  }
}
