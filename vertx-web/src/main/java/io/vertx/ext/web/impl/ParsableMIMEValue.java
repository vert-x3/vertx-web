package io.vertx.ext.web.impl;

import io.vertx.ext.web.MIMEHeader;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParsableMIMEValue extends ParsableHeaderValue implements MIMEHeader {

  private String component;
  private String subComponent;

  private int orderWeight;

  public ParsableMIMEValue(String headerContent) {
    super(headerContent);
    component = null;
    subComponent = null;
  }

  @Override
  public String component() {
    ensureHeaderProcessed();
    return component;
  }

  @Override
  public String subComponent() {
    ensureHeaderProcessed();
    return subComponent;
  }

  @Override
  public String mediaType() {
    ensureHeaderProcessed();
    return component + "/" + subComponent;
  }

  @Override
  public String mediaTypeWithParams() {
    ensureHeaderProcessed();
    StringBuilder sb = new StringBuilder(component + "/" + subComponent);
    if (!parameters().isEmpty()) {
      sb.append("; ");
      sb.append(parameters().entrySet().stream().map(ParsableMIMEValue::encodeParam).collect(Collectors.joining("; ")));
    }

    return sb.toString();
  }


  @Override
  protected boolean isMatchedBy2(ParsableHeaderValue matchTry) {
    ParsableMIMEValue myMatchTry = (ParsableMIMEValue) matchTry;
    ensureHeaderProcessed();

    if (!"*".equals(component) && !"*".equals(myMatchTry.component) && !component.equals(myMatchTry.component)) {
      return false;
    }
    if (!"*".equals(subComponent) && !"*".equals(myMatchTry.subComponent) && !subComponent.equals(myMatchTry.subComponent)) {
      return false;
    }

    if ("*".equals(component) && "*".equals(subComponent) && parameters().size() == 0) {
      return true;
    }

    return super.isMatchedBy2(myMatchTry);
  }

  @Override
  protected void ensureHeaderProcessed() {
    super.ensureHeaderProcessed();
    if (component == null) {
      HeaderParser.parseMIME(
        value,
        this::setComponent,
        this::setSubComponent
      );
      orderWeight = "*".equals(component) ? 0 : 1;
      orderWeight += "*".equals(subComponent) ? 0 : 2;
    }
  }

  public ParsableMIMEValue forceParse() {
    ensureHeaderProcessed();
    return this;
  }

  private void setComponent(String component) {
    this.component = "*".equals(component) ? "*" : component;
  }

  private void setSubComponent(String subComponent) {
    this.subComponent = "*".equals(subComponent) ? "*" : subComponent;
  }

  @Override
  protected int weightedOrderPart2() {
    return orderWeight;
  }

  private static final Pattern SPECIAL_CHARACTERS_IN_PARAM_VALUE = Pattern.compile(".*[\\s,;=\"].*");

  /**
   * Encodes a MIME parameter.
   * <p>
   * This method takes a parameter entry and encodes it into a string format suitable for MIME headers.
   * If the parameter value is null or blank, it returns just the key.
   * If the value is enclosed in double quotes, it returns the key and value as is.
   * If the value contains special characters, it encloses the value in double quotes.
   * Otherwise, it returns the key and value in a key=value format.
   * @param param The parameter entry to encode.
   * @return The encoded parameter string.
   */
  private static String encodeParam(Map.Entry<String, String> param) {
    // Check if the parameter value is null or blank
    if (param.getValue() == null || param.getValue().isBlank()) {
      // Return just the key if value is null or blank
      return param.getKey();
    } else {
      String value = param.getValue();
      // Check if the value is enclosed in double quotes
      if (value.startsWith("\"") && value.endsWith("\"")) {
        // Return the key and value as is
        return param.getKey() + "=" + value;
        // Check if the value contains special characters
      } else if (SPECIAL_CHARACTERS_IN_PARAM_VALUE.matcher(value).matches()) {
        // Escape quotes
        value = value.replace("\"", "\\\"");
        // Enclose the value in double quotes
        return param.getKey() + "=\"" + value + "\"";
      } else {
        // Return the key and value in key=value format
        return param.getKey() + "=" + param.getValue();
      }
    }
  }
}
