package io.vertx.ext.apex.handler;

import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.CookieDecoder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.ApexTestBase;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Crypto;
import io.vertx.ext.apex.handler.impl.CookieHandlerImpl;
import io.vertx.ext.apex.impl.AesHmacCodec;
import io.vertx.ext.apex.impl.AesHmacCodec.AesAlgorithm;
import io.vertx.ext.apex.impl.AesHmacCodec.MacAlgorithm;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.junit.Test;

public class EncryptedCookieHandlerTest extends ApexTestBase {

  private Crypto codec;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    codec = AesHmacCodec.build(AesAlgorithm.AES_128, "aesPassphrase", MacAlgorithm.HMAC_SHA_256, "macPassphrase", "saltForKeyGenerator");
    router.route().handler(new CookieHandlerImpl(codec));
  }

  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(rc -> {
      assertEquals(1, rc.cookieCount());
      Cookie cookie = rc.getCookie("foo");
      assertNotNull(cookie);
      assertEquals("bar", cookie.getValue());
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }

  @Test
  public void testGetCookies() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.cookieCount());
      Set<Cookie> cookies = rc.cookies();
      assertTrue(contains(cookies, "foo"));
      assertTrue(contains(cookies, "wibble"));
      assertTrue(contains(cookies, "plop"));
      rc.removeCookie("foo");
      cookies = rc.cookies();
      assertFalse(contains(cookies, "foo"));
      assertTrue(contains(cookies, "wibble"));
      assertTrue(contains(cookies, "plop"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(0, cookies.size());
    }, 200, "OK", null);
  }

  private boolean contains(Set<Cookie> cookies, String name) {
    for (Cookie cookie: cookies) {
      if (cookie.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testCookiesChangedInHandler() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.cookieCount());
      assertEquals("bar", rc.getCookie("foo").getValue());
      assertEquals("blibble", rc.getCookie("wibble").getValue());
      assertEquals("flop", rc.getCookie("plop").getValue());
      rc.removeCookie("plop");
      assertEquals(2, rc.cookieCount());
      rc.next();
    });
    router.route().handler(rc -> {
      assertEquals(2, rc.cookieCount());
      assertEquals("bar", rc.getCookie("foo").getValue());
      assertEquals("blibble", rc.getCookie("wibble").getValue());
      assertNull(rc.getCookie("plop"));
      rc.addCookie(Cookie.cookie("fleeb", "floob"));
      assertEquals(3, rc.cookieCount());
      assertNull(rc.removeCookie("blarb"));
      assertEquals(3, rc.cookieCount());
      Cookie foo = rc.getCookie("foo");
      foo.setValue("blah");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      // can't assert on the content because it's encrytped 
    }, 200, "OK", null);
  }

  /**
   * override this method so that Cookies are properly encrypted
   */
  @Override
  protected void testRequestBuffer(HttpClient client, HttpMethod method, int port, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    HttpClientRequest req = client.request(method, port, "localhost", path, resp -> {
      assertEquals(statusCode, resp.statusCode());
      assertEquals(statusMessage, resp.statusMessage());
      if (responseAction != null) {
        responseAction.accept(resp);
      }
      if (responseBodyBuffer == null) {
        latch.countDown();
      } else {
        resp.bodyHandler(buff -> {
          assertEquals(responseBodyBuffer, buff);
          latch.countDown();
        });
      }
    });
    if (requestAction != null) {
      requestAction.accept(req);
    }
    encryptCookies(req);
    req.end();
    awaitLatch(latch);
  }
  
  private void encryptCookies(HttpClientRequest req) throws Exception {
    // encrypt the value of unencrypted cookies
    String cookieHeader = req.headers().get("cookie");
    if (cookieHeader!=null) {
      Set<io.netty.handler.codec.http.Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
      for (io.netty.handler.codec.http.Cookie nettyCookie: nettyCookies) {
        String encryptedValue = codec.encrypt(nettyCookie.getValue());
        nettyCookie.setValue(encryptedValue);
      }
      // set the request header with encrypted cookies
      String newCookieHeader = ClientCookieEncoder.encode(nettyCookies);
      req.putHeader("cookie", newCookieHeader);
    }
  }

}
