package io.vertx.ext.web.client.tests;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.vertx.ext.web.client.spi.CookieStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CookieStoreTest {

  private CookieStore store;

  @BeforeEach
  public void setUp() {
    store = CookieStore.build();
  }

  private static Cookie cookie(String name, String value, String domain, String path) {
    DefaultCookie c = new DefaultCookie(name, value);
    c.setDomain(domain);
    if (path != null) {
      c.setPath(path);
    }
    return c;
  }

  private static List<Cookie> toList(Iterable<Cookie> iterable) {
    List<Cookie> list = new ArrayList<>();
    for (Cookie c : iterable) {
      list.add(c);
    }
    return list;
  }

  @Nested
  class PutAndGet {

    @Test
    public void testPutAndGetBasic() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("a", result.get(0).name());
      assertEquals("1", result.get(0).value());
    }
  }

  @Nested
  class DomainMatching {

    @Test
    public void testExactDomain() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("example.com", "/"));
      assertEquals(1, result.size());
    }

    @Test
    public void testSubdomainMatchesParent() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("www.example.com", "/"));
      assertEquals(1, result.size());
    }

    @Test
    public void testDifferentDomainDoesNotMatch() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("other.com", "/"));
      assertEquals(0, result.size());
    }

    @Test
    public void testNoDotBoundaryDoesNotMatch() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("examplefoo.com", "/"));
      assertEquals(0, result.size());
    }

    @Test
    public void testParentDoesNotMatchChild() {
      store.put(cookie("a", "1", "www.example.com", "/"));
      List<Cookie> result = toList(store.get("example.com", "/"));
      assertEquals(0, result.size());
    }

    @Test
    public void testMultipleSubdomainMatchesParent() {
      store.put(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get("a.b.c.example.com", "/"));
      assertEquals(1, result.size());
    }
  }

  @Nested
  class PathMatching {

    @Test
    public void testExactPath() {
      store.put(cookie("a", "1", "example.com", "/foo"));
      List<Cookie> result = toList(store.get("example.com", "/foo"));
      assertEquals(1, result.size());
    }

    @Test
    public void testSubPathMatches() {
      store.put(cookie("a", "1", "example.com", "/foo"));
      List<Cookie> result = toList(store.get("example.com", "/foo/bar"));
      assertEquals(1, result.size());
    }

    @Test
    public void testNoSlashBoundaryDoesNotMatch() {
      store.put(cookie("a", "1", "example.com", "/foo"));
      List<Cookie> result = toList(store.get("example.com", "/foobar"));
      assertEquals(0, result.size());
    }

    @Test
    public void testDifferentPathDoesNotMatch() {
      store.put(cookie("a", "1", "example.com", "/foo"));
      List<Cookie> result = toList(store.get("example.com", "/other"));
      assertEquals(0, result.size());
    }

    @Test
    public void testRootPathMatchesAll() {
      store.put(cookie("a", "1", "example.com", "/"));
      assertEquals(1, toList(store.get("example.com", "/")).size());
      assertEquals(1, toList(store.get("example.com", "/foo")).size());
      assertEquals(1, toList(store.get("example.com", "/foo/bar")).size());
    }

    @Test
    public void testShorterPathDoesNotMatch() {
      store.put(cookie("a", "1", "example.com", "/foo/bar"));
      List<Cookie> result = toList(store.get("example.com", "/foo"));
      assertEquals(0, result.size());
    }
  }

  @Nested
  class Replacement {

    @Test
    public void testSameNameDomainPathReplaces() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("a", "2", "example.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("2", result.get(0).value());
    }

    @Test
    public void testSameNameDifferentDomainStoresBoth() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("a", "2", "other.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("1", result.get(0).value());
      result = toList(store.get( "other.com", "/"));
      assertEquals(1, result.size());
      assertEquals("2", result.get(0).value());
    }

    @Test
    public void testSameNameDifferentPathStoresBoth() {
      store.put(cookie("a", "1", "example.com", "/foo"));
      store.put(cookie("a", "2", "example.com", "/bar"));
      List<Cookie> result = toList(store.get( "example.com", "/foo"));
      assertEquals(1, result.size());
      assertEquals("1", result.get(0).value());
      result = toList(store.get( "example.com", "/bar"));
      assertEquals(1, result.size());
      assertEquals("2", result.get(0).value());
    }
  }

  @Nested
  class Remove {

    @Test
    public void testRemoveCookie() {
      Cookie c = cookie("a", "1", "example.com", "/");
      store.put(c);
      assertEquals(1, toList(store.get( "example.com", "/")).size());
      store.remove(c);
      assertEquals(0, toList(store.get( "example.com", "/")).size());
    }

    @Test
    public void testRemoveNonExistentDoesNotThrow() {
      Cookie c = cookie("a", "1", "example.com", "/");
      assertDoesNotThrow(() -> store.remove(c));
    }

    @Test
    public void testRemoveOnlyAffectsMatchingCookie() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "example.com", "/"));
      store.remove(cookie("a", "1", "example.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("b", result.get(0).name());
    }
  }

  @Nested
  class GetByName {

    @Test
    public void testGetByNameReturnsMatch() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "example.com", "/"));
      Cookie result = store.get("a", "example.com", "/");
      assertNotNull(result);
      assertEquals("a", result.name());
      assertEquals("1", result.value());
    }

    @Test
    public void testGetByNameReturnsNullWhenNoMatch() {
      store.put(cookie("a", "1", "example.com", "/"));
      Cookie result = store.get("b", "example.com", "/");
      assertNull(result);
    }

    @Test
    public void testGetByNameReturnsNullWhenEmpty() {
      Cookie result = store.get("a", "example.com", "/");
      assertNull(result);
    }
  }

  @Nested
  class MultipleCookies {

    @Test
    public void testMultipleNamesReturnedForSameDomainPath() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "example.com", "/"));
      store.put(cookie("c", "3", "example.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(3, result.size());
    }

    @Test
    public void testDifferentDomainsIsolated() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "other.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("a", result.get(0).name());
    }

    @Test
    public void testSubdomainGetsCookiesFromParentAndSelf() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "www.example.com", "/"));
      List<Cookie> result = toList(store.get( "www.example.com", "/"));
      assertEquals(2, result.size());
    }

    @Test
    public void testParentDoesNotGetSubdomainCookies() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "www.example.com", "/"));
      List<Cookie> result = toList(store.get( "example.com", "/"));
      assertEquals(1, result.size());
      assertEquals("a", result.get(0).name());
    }

    @Test
    public void testPathFilteringWithMultiplePaths() {
      store.put(cookie("a", "1", "example.com", "/"));
      store.put(cookie("b", "2", "example.com", "/foo"));
      store.put(cookie("c", "3", "example.com", "/foo/bar"));
      assertEquals(3, toList(store.get( "example.com", "/foo/bar")).size());
      assertEquals(2, toList(store.get( "example.com", "/foo")).size());
      assertEquals(1, toList(store.get( "example.com", "/")).size());
    }
  }
}
