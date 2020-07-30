package io.vertx.ext.web.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import io.vertx.ext.web.ParsedHeaderValue;

public class ParsableHeaderValueTest {

  private ParsableHeaderValue headerValue;
  
  @Before
  public void initialize() {
    headerValue = new ParsableHeaderValue("application/json; charset=UTF-8");
  }
  
  @Test
  public void testIsMatchedByEqualsCases() {
    ParsedHeaderValue value = new ParsableMIMEValue("application/json; charset=UTF-8").forceParse();
    assertTrue(headerValue.isMatchedBy(value));
  }
  
  @Test
  public void testIsMatchedByDiffCases() {
    ParsedHeaderValue value = new ParsableMIMEValue("application/json; charset=utf-8").forceParse();
    assertTrue(headerValue.isMatchedBy(value));
  }
  
  @Test
  public void testIsMatchedByDiff() {
    ParsedHeaderValue value = new ParsableMIMEValue("application/json; charset=UTF-16").forceParse();
    assertFalse(headerValue.isMatchedBy(value));
  }
  
}
