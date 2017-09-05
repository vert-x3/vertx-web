package io.vertx.ext.web.impl;

import io.vertx.ext.web.MIMEHeader;

public class ParsableMIMEValue extends ParsableHeaderValue implements MIMEHeader{

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
    return component;
  }

  @Override
  public String subComponent() {
    return subComponent;
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
    if(component == null){
      HeaderParser.parseMIME(
            value,
            this::setComponent,
            this::setSubComponent
          );
      orderWeight = "*".equals(component) ? 0 : 1;
      orderWeight += "*".equals(subComponent) ? 0 : 2;
    }
  }

  public ParsableMIMEValue forceParse(){
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
  protected int weightedOrderPart2(){
    return orderWeight;
  }
}
