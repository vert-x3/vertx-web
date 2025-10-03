package io.vertx.ext.web.tests.impl;

import io.vertx.ext.web.impl.Origin;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class OriginTests {

  @RunWith(Parameterized.class)
  public static class ValidOriginTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(new Object[][]{
        {"null"},
        {"http://www.vertx.io"},
        {"http://[::1]"},
        {"http://my-site.com"},
        {"http://my-site.123.com"},
        {"chrome-extension://gmbgaklkmjakoegficnlkhebmhkjfich"},
        {"moz-extension://" + UUID.randomUUID()}
      });
    }

    private final String origin;

    public ValidOriginTest(String origin) {
      this.origin = origin;
    }

    @Test
    public void testValidOrigin() {
      Origin.parse(origin); // does not fail
      assertTrue(Origin.isValid(origin));
    }
  }


  @RunWith(Parameterized.class)
  public static class InvalidOriginTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(new Object[][]{
        {"httpss://www.google.com"},
        {"http://1[::1]:8080"},
        {"http://my_site.com"},
        {"http://1[::1]:8080"},
        {"chrome-extension://gmbg0klkmjakoegficnlkhebmhkjfich"},
        {"chrome-extension://gmbgaklkmjako"},
        {"moz-extension://" + UUID.randomUUID().toString().replace('-', 'z')},
        {"moz-extension://" + UUID.randomUUID().toString().substring(1)},
      });
    }

    private final String origin;

    public InvalidOriginTest(String origin) {
      this.origin = origin;
    }

    @Test
    public void testInvalidOrigin() {
      try {
        Origin.parse(origin);
        fail("Should fail as it is a invalid origin: " + origin);
      } catch (RuntimeException e) {
        // OK
      }

      assertFalse(Origin.isValid(origin));
    }
  }
}
