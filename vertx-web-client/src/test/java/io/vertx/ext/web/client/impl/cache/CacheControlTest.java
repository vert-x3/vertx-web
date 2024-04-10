package io.vertx.ext.web.client.impl.cache;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Test;

import io.vertx.core.MultiMap;

public class CacheControlTest {
  @Test
  public void testMaxAge() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("Date", "Tue, 09 Apr 2024 19:10:51 GMT");
    headers.add("Expires", "Thu, 09 May 2024 19:10:51 GMT");

    CacheControl cc = CacheControl.parse(headers);
    assertEquals(Duration.ofDays(30).getSeconds(), cc.getMaxAge());
  }

  @Test
  public void testInvalidHeaders() {
    // invalid Date
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("Date", "this header doesn't make sense");
    headers.add("Expires", "Thu, 09 May 2024 19:10:51 GMT");

    CacheControl cc = CacheControl.parse(headers);
    assertEquals(Long.MAX_VALUE, cc.getMaxAge());

    // invalid Expires
    headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("Date", "Tue, 09 Apr 2024 19:10:51 GMT");
    headers.add("Expires", "this header doesn't make sense");

    cc = CacheControl.parse(headers);
    assertEquals(Long.MAX_VALUE, cc.getMaxAge());
  }
}
