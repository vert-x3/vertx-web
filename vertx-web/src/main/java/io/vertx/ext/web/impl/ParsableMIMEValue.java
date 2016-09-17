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
    
    System.out.println(component + "/" + subComponent);
    if (component != STAR && myMatchTry.component != STAR && !component.equals(myMatchTry.component)) {
      return false;
    }
    if (subComponent != STAR && myMatchTry.subComponent != STAR && !subComponent.equals(myMatchTry.subComponent)) {
      return false;
    }
    return super.isMatchedBy2(myMatchTry);
  }
  
  @Override
  protected void ensureHeaderProcessed() {
    super.ensureHeaderProcessed();
    if(component == null){
      HeaderParser.parseMIME(
            value(),
            this::setComponent,
            this::setSubComponent
          );
      orderWeight = STAR == component ? 0 : 1;
      orderWeight += STAR == subComponent ? 0 : 2;
    }
  }
  
  public ParsableMIMEValue forceParse(){
    ensureHeaderProcessed();
    return this;
  }
  
  private void setComponent(String component) {
    this.component = STAR.equals(component) ? STAR : component;
  }
  
  private void setSubComponent(String subComponent) {
    this.subComponent = STAR.equals(subComponent) ? STAR : subComponent;
  }
    
  @Override
  protected int weightedOrderPart2(){
    return orderWeight;
  }
}
