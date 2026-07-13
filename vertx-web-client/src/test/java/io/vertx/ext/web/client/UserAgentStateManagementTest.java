package io.vertx.ext.web.client;

import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.internal.PlatformDependent;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.*;
import io.vertx.test.tls.Cert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class UserAgentStateManagementTest {

  private Vertx vertx;
  private HttpServer plainTextServer;
  private HttpServer secureServer;
  private WebClient client;
  private WebClientSession session;
  private volatile When stack;

  @Before
  public void before() {

    Vertx vertx = Vertx.vertx(new VertxOptions()
        .setAddressResolverOptions(new AddressResolverOptions().
          setHostsValue(Buffer.buffer(
            "127.0.0.1 example.com\n" +
            "127.0.0.1 www.example.com\n" +
            "127.0.0.1 examplefoo.com\n" +
            "127.0.0.1 other.com\n" +
            "127.0.0.1 localhost\n"))));

    Handler<HttpServerRequest> requestHandler = request -> {
      When w = stack;
      if (w == null) {
        request.response().setStatusCode(500).end();
      } else {

        // Find clause to execute
        while (w.parent != null && !w.parent.processed) {
          w = w.parent;
        }

        if (
          w.secure != request.isSSL() ||
          !w.host.equals(request.authority().host()) ||
          w.port != request.authority().port() ||
          !w.path.equals(request.path())) {
          request.response().setStatusCode(404).end();
          return;
        }

        w.processed = true;

        Consumer<Cookies> cookiesAssert = w.cookiesAssert;
        if (cookiesAssert != null) {
          try {
            cookiesAssert.accept(new Cookies(request));
          } catch (Throwable failure) {
            request.response().setStatusCode(500).end();
            return;
          }
        }

        List<SetCookie> setCookieList = w.setCookieList;
        if (setCookieList != null) {
          for (SetCookie setCookie : setCookieList) {
            io.vertx.core.http.Cookie cookie = io.vertx.core.http.Cookie.cookie(setCookie.name, setCookie.value);
            if (setCookie.path != null) {
              cookie.setPath(setCookie.path);
            }
            if (setCookie.domain != null) {
              cookie.setDomain(setCookie.domain);
            }
            cookie.setSecure(setCookie.secure);
            if (setCookie.maxAge != null) {
              cookie.setMaxAge(setCookie.maxAge);
            }
            request.response().headers().add(HttpHeaders.SET_COOKIE, cookie.encode());
          }
        }
        Integer sc = w.statusCode;
        if (sc != null) {
          HttpServerResponse response = request.response();
          if (w.location != null) {
            response.putHeader(HttpHeaders.LOCATION, w.location);
          }
          response
            .setStatusCode(sc)
            .end();
        }
      }
    };
    HttpServer plainTextServer = vertx.createHttpServer()
      .requestHandler(requestHandler);

    await(plainTextServer.listen(8080, "localhost"));

    HttpServer secureServer = vertx.createHttpServer(new HttpServerOptions().setSsl(true).setKeyCertOptions(Cert.SERVER_JKS.get()))
      .requestHandler(requestHandler);

    await(secureServer.listen(8081, "localhost"));

    WebClientOptions options = new WebClientOptions()
      .setDefaultPort(80)
      .setDefaultHost("another.com")
      .setVerifyHost(false)
      .setTrustAll(true)
      .setFollowRedirects(true);

    this.vertx = vertx;
    this.plainTextServer = plainTextServer;
    this.secureServer = secureServer;
    this.client = WebClient.create(vertx, options);
    this.session = WebClientSession.create(client);
  }

  @After
  public void after() throws Exception {
    vertx
      .close()
      .toCompletionStage()
      .toCompletableFuture()
      .get(20, TimeUnit.SECONDS);
  }

  @Test
  public void testPathMatchExact() {
    when("http://example.com:8080/foo")
      .set(cookie -> cookie.name("a").value("1").path("/foo"))
      .respond(200);
    when("http://example.com:8080/foo")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testPathMatchWithSubpath() {
    when("http://example.com:8080/foo")
      .set(cookie -> cookie.name("a").value("1").path("/foo"))
      .respond(200);
    when("http://example.com:8080/foo/bar")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testPathMatchDifferentPath() {
    when("http://example.com:8080/foo")
      .set(cookie -> cookie.name("a").value("1").path("/foo"))
      .respond(200);
    when("http://example.com:8080/other")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testDefaultPathWithSingleSegment() {
    when("http://example.com:8080/foo")
      .set(cookie -> cookie.name("a").value("1"))
      .respond(200);
    when("http://example.com:8080/foo")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testDefaultPathWithTwoSegments() {
    when("http://example.com:8080/foo/bar")
      .set(cookie -> cookie.name("a").value("1"))
      .respond(200);
    when("http://example.com:8080/foo/baz")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/foobar")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/other")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testDefaultDomain() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
    when("http://other.com:8080/other")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testDomainMatching() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://other.com:8080/other")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testDomainMatchingDoesNotMatchWithoutDotBoundary() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .respond(200);
    when("http://examplefoo.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testSetCookieInvalidDomain() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("other.com"))
      .respond(200);
    when("http://other.com:8080/other")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testReplaceDomainCookieWithHostOnly() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("2"))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testReplaceHostOnlyCookieWithDomain() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1"))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("2").domain("example.com"))
      .respond(200);
    when("http://www.example.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
  }

  @Test
  public void testSetCookieParentDomain() {
    when("http://www.example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
      })
      .respond(200);
  }

  @Test
  public void testOrderingOfSameNameCookiesWithDifferentPath() {
    testOrderingOfCookie((idx, cookie) -> cookie
      .name("test")
      .path(repeat("/c", idx))
      .value("value_" + idx));
  }

  @Test
  public void testOrderingOfCookiesWithDifferentPath() {
    testOrderingOfCookie((idx, cookie) -> cookie
      .name("test_" + idx)
      .path(repeat("/c", idx))
      .value("value_" + idx));
  }

  void testOrderingOfCookie(BiConsumer<Integer, SetCookie> setCookie) {
    int num = 3;
    List<Integer> order = IntStream.range(1, 1 + num)
      .boxed()
      .collect(Collectors.toList());
    Collections.shuffle(order);
    for (int i = 0;i < num;i++) {
      int idx = order.get(i);
      String path = repeat("/c", idx);
      when("http://example.com:8080" + path)
        .set(cookie -> setCookie.accept(idx, cookie))
        .respond(200);
    }
    String path = repeat("/c",1 + num);
    when("http://example.com:8080" + path)
      .assertThat(cookies -> {
        List<Cookie> testCookies = cookies.get();
        assertEquals(num, testCookies.size());
        Cookie testCookie = testCookies.get(0);
        assertEquals("value_" + num, testCookie.value);
      })
      .respond(200);
  }

  @Test
  public void testOrderingOfCookiesWithDifferentCreationDate() {
    // Shuffle nom names
    List<String> names = new ArrayList<>(Arrays.asList("a", "b", "c"));
    Collections.shuffle(names);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name(names.get(0)).value("1").domain("example.com"))
      .respond(200);
    pause(1);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name(names.get(1)).value("2").domain("example.com"))
      .respond(200);
    pause(1);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name(names.get(2)).value("3").domain("example.com"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(3, cookies.get().size());
        assertEquals(names.get(0), cookies.get().get(0).name);
        assertEquals(names.get(1), cookies.get().get(1).name);
        assertEquals(names.get(2), cookies.get().get(2).name);
      })
      .respond(200);
  }

  @Test
  public void testOrderingOfCookiesWithDifferentPathRegardlessOfDomain() {
    testOrderingOfCookiesWithDifferentPathRegardlessOfDomain(0);
    testOrderingOfCookiesWithDifferentPathRegardlessOfDomain(2);
  }

  private void testOrderingOfCookiesWithDifferentPathRegardlessOfDomain(int r) {
    for (int i = 0;i < 2;i++) {
      int idx = r + i;
      switch (idx) {
        case 0:
        case 3:
          when("http://example.com:8080/")
            .set(cookie -> cookie.name("a").path("/c/c").value("2").domain("example.com"))
            .respond(200);
          break;
        case 1:
        case 2:
          when("http://www.example.com:8080/")
            .set(cookie -> cookie.name("b").path("/c").value("2"))
            .respond(200);
          break;
      }
    }
    when("http://www.example.com:8080/c/c")
      .assertThat(cookies -> {
        assertEquals(2, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
        assertEquals("b", cookies.get().get(1).name);
      })
      .respond(200);
  }

  @Test
  public void testInsertionOrderOfSameCookiesRegardlessOf() {
    testInsertionOrderOfSameCookiesRegardlessOf("example.com,example.com", "example.com,");
    testInsertionOrderOfSameCookiesRegardlessOf(",example.com", ",");
  }

  private void testInsertionOrderOfSameCookiesRegardlessOf(String d1, String d2) {
    when("http://example.com:8080/")
      .set(cookie -> {
        cookie.name("foo").value("1");
        if (d1 != null && !d1.isEmpty()) {
          cookie.domain(d1);
        }
      })
      .set(cookie -> {
        cookie.name("foo").value("2");
        if (d2 != null && !d2.isEmpty()) {
          cookie.domain(d2);
        }
      })
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
        assertEquals("foo", cookies.get().get(0).name);
        assertEquals("2", cookies.get().get(0).value);
      })
      .respond(200);
  }

  @Test
  public void testCreationDateIsPreserved() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .respond(200);
    pause(1);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("b").value("2").domain("example.com"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(2, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
        assertEquals("b", cookies.get().get(1).name);
      })
      .respond(200);
    pause(1);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("3").domain("example.com"))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(2, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
        assertEquals("b", cookies.get().get(1).name);
      })
      .respond(200);
  }

  @Test
  public void testSecureAttribute() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com").secure(true))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(0, cookies.get().size());
      })
      .respond(200);
    when("https://example.com:8081/")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
      })
      .respond(200);
  }

  @Test
  public void testMaxAge() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com").maxAge(10))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
      })
      .respond(200);
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com").maxAge(0))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(0, cookies.get().size());
      })
      .respond(200);
  }

  @Test
  public void testMaxAgeExpiration() {
    when("http://example.com:8080/")
      .set(cookie -> cookie.name("a").value("1").domain("example.com").maxAge(1))
      .respond(200);
    pause(1000);
    when("http://example.com:8080/")
      .assertThat(cookies -> {
        assertEquals(0, cookies.get().size());
      })
      .respond(200);
  }

  @Test
  public void testRedirectToSameDomain() {
    when("http://example.com:8080/redirect")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .set(cookie -> cookie.name("b").value("2"))
      .redirect("http://example.com:8080/target")
      .assertThat(cookies -> {
        assertEquals(2, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
        assertEquals("b", cookies.get().get(1).name);
      })
      .respond(200);
  }

  @Test
  public void testRedirectToSubDomain() {
    when("http://example.com:8080/redirect")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .set(cookie -> cookie.name("b").value("2"))
      .redirect("http://www.example.com:8080/target")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
      })
      .respond(200);
  }

  @Test
  public void testRedirectToParentDomain() {
    when("http://www.example.com:8080/redirect")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .set(cookie -> cookie.name("b").value("2"))
      .redirect("http://example.com:8080/target")
      .assertThat(cookies -> {
        assertEquals(1, cookies.get().size());
        assertEquals("a", cookies.get().get(0).name);
      })
      .respond(200);
  }

  @Test
  public void testRedirectToOtherDomain() {
    when("http://www.example.com:8080/redirect")
      .set(cookie -> cookie.name("a").value("1").domain("example.com"))
      .set(cookie -> cookie.name("b").value("2"))
      .redirect("http://other.com:8080/target")
      .assertThat(cookies -> {
        assertEquals(0, cookies.get().size());
      })
      .respond(200);
  }

  @Test
  public void testRedirectTargetSetsCookieScopedToTargetHost() {
    when("http://example.com:8080/redirect")
      .redirect("http://other.com:8080/target")
      .set(cookie -> cookie.name("a").value("1"))
      .respond(200);
    when("http://other.com:8080/")
      .assertThat(cookies -> assertEquals(1, cookies.get("a").size()))
      .respond(200);
    when("http://example.com:8080/")
      .assertThat(cookies -> assertEquals(0, cookies.get("a").size()))
      .respond(200);
  }

  private When when(String uri) {
    return new When(uri);
  }

  private class When {

    private final When parent;
    private final boolean secure;
    private final String host;
    private final String path;
    private final int port;
    private List<SetCookie> setCookieList;
    private Consumer<Cookies> cookiesAssert;
    private volatile boolean processed;

    // The server action
    private Integer statusCode;
    private String location;

    private When(String uri) {
      this(null, uri);
    }

    private When(When parent, String uri) {

      URI parsed = URI.create(uri);

      boolean secure;
      switch (parsed.getScheme()) {
        case "http":
          secure = false;
          break;
        case "https":
          secure = true;
          break;
        default:
          throw new IllegalArgumentException();
      }

      this.secure = secure;
      this.path = parsed.getPath();
      this.port = parsed.getPort();
      this.host = parsed.getHost();
      this.parent = parent;
    }

    public When set(Consumer<SetCookie> consumer) {
      if (setCookieList == null) {
        setCookieList = new ArrayList<>();
      }
      SetCookie setCookie = new SetCookie();
      consumer.accept(setCookie);
      setCookieList.add(setCookie);
      return this;
    }

    public When redirect(String to) {
      location = to;
      statusCode = 302;
      return new When(this, to);
    }

    public void respond(int sc) {
      if (Context.isOnVertxThread()) {
        throw new UnsupportedOperationException();
      }
      if (stack != null) {
        throw new IllegalStateException();
      }
      statusCode = sc;
      stack = this;

      // Find root to execute
      When root = this;
      while (root.parent != null) {
        root = root.parent;
      }

      try {
        HttpResponse<Buffer> response = await(session
          .get(root.port, root.host, root.path)
          .ssl(root.secure)
          .send());
        assertEquals(200, response.statusCode());
      } finally {
        stack = null;
      }
    }
    private When assertThat(Consumer<Cookies> consumer) {
      cookiesAssert = consumer;
      return this;
    }
  }

  private static class Cookies {

    private final String cookieHeader;

    private Cookies(HttpServerRequest request) {

      this.cookieHeader = request.getHeader(HttpHeaders.COOKIE);

    }

    List<Cookie> get(Predicate<Cookie> filter) {
      if (cookieHeader != null) {
        List<Cookie> list = ServerCookieDecoder.STRICT.decodeAll(cookieHeader)
          .stream().map(Cookie::new).filter(filter).collect(Collectors.toList());
        return list;
      } else {
        return Collections.emptyList();
      }
    }

    List<Cookie> get() {
      return get(cookie -> true);
    }

    List<Cookie> get(String name) {
      return get(cookie -> name.equals(cookie.name));
    }
  }

  static class Cookie {
    final String name;
    final String value;
    private Cookie(io.netty.handler.codec.http.cookie.Cookie cookie) {
      this.name =  cookie.name();
      this.value = cookie.value();
    }
  }


  private static class SetCookie {
    private String name;
    private String value;
    private String path;
    private String domain;
    private boolean secure;
    private Long maxAge;
    SetCookie name(String name) {
      this.name = name;
      return this;
    }
    SetCookie value(String value) {
      this.value = value;
      return this;
    }
    SetCookie path(String path) {
      this.path = path;
      return this;
    }
    SetCookie domain(String domain) {
      this.domain = domain;
      return this;
    }
    SetCookie secure(boolean secure) {
      this.secure = secure;
      return this;
    }
    SetCookie maxAge(long maxAge) {
      this.maxAge = maxAge;
      return this;
    }
  }

  private static void pause(long ms) {
    Assert.assertFalse(Context.isOnVertxThread());
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static <T> T await(Future<T> f) {
    try {
      return f.toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
      throw new AssertionError();
    }
  }

  private static String repeat(String s, int times) {
    StringBuilder sb = new StringBuilder(s.length() * times);
    for (int i = 0;i < times;i++) {
      sb.append(s);
    }
    return sb.toString();
  }

}
