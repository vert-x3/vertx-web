package io.vertx.ext.web.impl;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class OriginTests {

  @RunWith(Parameterized.class)
  public static class GoodOriginTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(new Object[][]{
        {"null"},
        {"http://www.vertx.io"},
        {"http://[::1]"},
        {"http://my-site.com"},
        {"http://my-site.123.com"},
        {"chrome-extension://gmbgaklkmjakoegficnlkhebmhkjfich"}
      });
    }

    private final String origin;

    public GoodOriginTest(String origin) {
      this.origin = origin;
    }

    @Test
    public void testGoodOrigin() {
      Origin.parse(origin); // does not fail
      assertTrue(Origin.isValid(origin));
    }
  }


  @RunWith(Parameterized.class)
  public static class BadOriginTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(new Object[][]{
        {"httpss://www.google.com"},
        {"http://1[::1]:8080"},
        {"http://my_site.com"},
        {"http://1[::1]:8080"},
        {"chrome-extension://gmbg0klkmjakoegficnlkhebmhkjfich"},
        {"chrome-extension://gmbgaklkmjako"},
      });
    }

    private final String origin;

    public BadOriginTest(String origin) {
      this.origin = origin;
    }

    @Test
    public void testBadOrigin() {
      try {
        Origin.parse(origin);
        fail("Should fail as it is a bad origin: " + origin);
      } catch (RuntimeException e) {
        // OK
      }

      assertFalse(Origin.isValid(origin));
    }
  }
}
