package io.vertx.ext.web;

import io.vertx.ext.web.impl.Signature;
import org.junit.Test;

import static org.junit.Assert.*;

public class SignatureTest {

  @Test
  public void verifiesItsOwnSignatures() {
    Signature signature = new Signature("any-string");
    final String token = signature.sign("my-random-bit-of-data");
    assertTrue(signature.verify(token));
  }

  @Test
  public void handlesMultiPeriodData() {
    Signature signature = new Signature("any-string");
    final String token = signature.sign("my-random-bit-of-data.some-other-data.bit");
    assertTrue(token.contains("my-random-bit-of-data.some-other-data.bit"));
    assertEquals(4, token.split("\\.").length);
    assertTrue(signature.verify(token));
  }

  @Test
  public void rejectsDataWithoutSignature() {
    Signature signature = new Signature("any-string");
    assertFalse(signature.verify("just-some-data-not-signed"));
  }

  @Test
  public void rejectsAlteredData() {
    Signature signature = new Signature("any-string");
    String token = signature.sign("my-random-bit-of-data");
    token = token.replaceFirst("random-bit", "edited-bit");
    assertFalse(signature.verify(token));
  }

  @Test
  public void parseSignedData() {
    Signature signature = new Signature("any-string");
    String token = signature.sign("my-random-bit-of-data");
    assertEquals("my-random-bit-of-data", signature.parse(token));
  }

  @Test
  public void parseAlteredSignedData() {
    Signature signature = new Signature("any-string");
    String token = signature.sign("my-random-bit-of-data");
    token = token.replaceFirst("random-bit", "edited-bit");
    assertNull(signature.parse(token));
  }
}
