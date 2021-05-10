package io.vertx.ext.web.impl;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class OriginTest {

  @Test
  public void testOrigin() {
    Origin.parse("http://www.vertx.io");
    assertTrue(Origin.isValid("http://www.vertx.io"));

    Origin.parse("http://[::1]");
    assertTrue(Origin.isValid("http://[::1]"));

    Origin.parse("http://my-site.com");
    assertTrue(Origin.isValid("http://my-site.com"));

    Origin.parse("http://my-site.123.com");
    assertTrue(Origin.isValid("http://my-site.123.com"));
  }

  @Test
  public void testBadOrigin() {
    List<String> origins = Arrays.asList(
      "httpss://www.google.com",
      "http://1[::1]:8080",
      "http://my_site.com",
      "http://1[::1]:8080"
    );

    for (String origin : origins) {
      try {
        Origin.parse(origin);
        fail("Should fail as it is a bad origin: " + origin);
      } catch (RuntimeException e) {
        // OK
      }

      assertFalse(Origin.isValid(origin));
    }
  }

  @Test
  public void testNullOrigin() {
    Origin.parse("null");
    assertTrue(Origin.isValid("null"));
  }

}
