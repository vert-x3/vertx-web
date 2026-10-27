package io.vertx.ext.web.client.tests;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.CachingWebClientOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CachingWebClientOptionsTest {

  @Test
  public void testAddingCachedStatusCodeDoesNotMutateDefaults() {
    CachingWebClientOptions options = new CachingWebClientOptions();
    options.addCachedStatusCode(500);

    assertTrue(options.getCachedStatusCodes().contains(500));
    assertFalse(CachingWebClientOptions.DEFAULT_CACHED_STATUS_CODES.contains(500));
    assertFalse(new CachingWebClientOptions().getCachedStatusCodes().contains(500));
  }

  @Test
  public void testRemovingCachedStatusCodeDoesNotMutateDefaults() {
    CachingWebClientOptions options = new CachingWebClientOptions();
    options.removeCachedStatusCode(200);

    assertFalse(options.getCachedStatusCodes().contains(200));
    assertTrue(CachingWebClientOptions.DEFAULT_CACHED_STATUS_CODES.contains(200));
    assertTrue(new CachingWebClientOptions().getCachedStatusCodes().contains(200));
  }

  @Test
  public void testDefaultsAreUnmodifiable() {
    assertThrows(UnsupportedOperationException.class, () -> CachingWebClientOptions.DEFAULT_CACHED_STATUS_CODES.add(500));
    assertThrows(UnsupportedOperationException.class, () -> CachingWebClientOptions.DEFAULT_CACHED_METHODS.add(HttpMethod.POST));
  }

  @Test
  public void testAddingCachedMethodDoesNotMutateDefaults() {
    CachingWebClientOptions options = new CachingWebClientOptions();
    options.addCachedMethod(HttpMethod.POST);

    assertTrue(options.getCachedMethods().contains(HttpMethod.POST));
    assertFalse(CachingWebClientOptions.DEFAULT_CACHED_METHODS.contains(HttpMethod.POST));
    assertFalse(new CachingWebClientOptions().getCachedMethods().contains(HttpMethod.POST));
  }
}
