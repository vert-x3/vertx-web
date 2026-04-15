package io.vertx.ext.web.client.tests;

import io.netty.util.internal.PlatformDependent;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.AsyncFileLock;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.htdigest.HtdigestCredentials;
import io.vertx.ext.web.client.*;
import io.vertx.ext.web.client.tests.jackson.WineAndCheese;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;


import io.vertx.test.core.TestUtils;
import io.vertx.test.fakeresolver.FakeAddress;
import io.vertx.test.fakeresolver.FakeAddressResolver;
import io.vertx.test.proxy.*;
import io.vertx.test.tls.Cert;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.assertj.core.api.Assertions;

import static io.vertx.test.core.AsyncTestBase.assertWaitUntil;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientTest extends WebClientTestBase {

  abstract class SendTestBase {

    boolean shutdownServer;
    WebClient webClient;
    HttpServer server;

    public SendTestBase() {
      this.webClient = null;
    }

    void init() throws Exception {
    }

    void requestBegin(HttpServerRequest req) {
    }

    void requestEnd(HttpServerRequest req, Buffer body) {
      requestEnd(req);
    }

    void requestEnd(HttpServerRequest req) {
    }

    void handleRequest(HttpServerRequest req) {
      boolean success1 = false;
      try {
        requestBegin(req);
        success1 = true;
      } finally {
        HttpServerResponse resp = req.response();
        if (!success1) {
          if (!resp.ended()) {
            resp
              .setStatusCode(400)
              .end();
          }
        } else {
          req.bodyHandler(body -> {
            boolean success2 = false;
            try {
              requestEnd(req, body);
              success2 = true;
            } finally {
              if (!success2) {
                resp.setStatusCode(400);
              }
              if (!resp.ended() && !resp.isChunked()) {
                resp.end();
              }
            }
          });
        }
      }
    }

    abstract Future<? extends HttpResponse<?>> send(WebClient client);

    void assertResponse(HttpResponse<?> response) {
      // Check we don't override
      Class<?> clazz = getClass();
      try {
        clazz.getDeclaredMethod("assertResponseFailure", Throwable.class);
      } catch (NoSuchMethodException e) {
        return;
      }
      fail("Was expecting a failure");
    }

    void assertResponseFailure(Throwable failure) {
      PlatformDependent.throwException(failure);
    }

    protected void bind() {
      shutdownServer = server != WebClientTest.this.server;
      server.listen().await();
    }

    @Test
    void run() throws Exception {
      webClient = WebClientTest.this.webClient;
      server = WebClientTest.this.server;
      init();
      server.requestHandler(this::handleRequest);
      bind();
      HttpResponse<?> response;
      try {
        WebClient client = this.webClient;
        response = send(client).await();
      } catch (Throwable err) {
        assertResponseFailure(err);
        return;
      } finally {
        if (shutdownServer) {
          server.close().await();
        }
      }
      assertResponse(response);
    }
  }

  @Nested
  class DefaultHostAndPortTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("localhost:8080", req.authority().toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somepath").send();
    }
  }

  @Nested
  class DefaultPortTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("somehost:8080", req.authority().toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").send();
    }
  }

  @Nested
  class DefaultUserAgentTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      String ua = req.headers().get(HttpHeaders.USER_AGENT);
      Assertions.assertThat(ua).startsWith("Vert.x-WebClient/");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").send();
    }
  }

  @Nested
  class BasicAuthenticationTest extends SendTestBase {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("ém$¨=!$€", "&@#§$*éà#\"'");
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      assertEquals(creds.toHttpAuthorization(), auth, "Was expecting authorization header to contain a basic authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").authentication(creds).send();
    }
  }

  @Nested
  class BasicAuthenticationEmptyUsernameTest extends SendTestBase {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("", "&@#§$*éà#\"'");
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      assertEquals(creds.toHttpAuthorization(), auth, "Was expecting authorization header to contain a basic authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").authentication(creds).send();
    }
  }

  @Nested
  class BasicAuthenticationNullUsernameTest extends SendTestBase {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(null, "&@#§$*éà#\"'");
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      assertEquals(creds.toHttpAuthorization(), auth, "Was expecting authorization header to contain a basic authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").authentication(creds).send();
    }
  }

  @Nested
  class BasicAuthenticationNullUsername2Test extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      assertEquals("Basic OnBhc3N3b3Jk", auth, "Was expecting authorization header to contain a basic authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").basicAuthentication("", "password").send();
    }
  }

  @Nested
  class BearerTokenAuthenticationTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      assertEquals("Bearer sometoken", auth, "Was expecting authorization header to contain a bearer token authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").authentication(new TokenCredentials("sometoken")).send();
    }
  }

  @Nested
  class DigestAuthenticationTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
      String expected = "Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\", qop=auth, nc=1, cnonce=\"0a4f113b\", response=\"95c727b8ed724ea2be8e9318e0e4f619\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
      assertEquals(expected, auth, "Was expecting authorization header to contain a digest authentication string");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "/dir/index.html")
        .authentication(
          // like on wikipedia
          new HtdigestCredentials("Mufasa", "Circle Of Life")
          .applyHttpChallenge(
            "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"",
            HttpMethod.GET,
            "/dir/index.html",
            1,
            "0a4f113b"
          )
        ).send();
    }
  }

  @Nested
  class CustomUserAgentTest extends SendTestBase {
    @Override
    void init() {
      webClient = WebClient.wrap(client, new WebClientOptions().setUserAgent("smith"));
    }

    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.singletonList("smith"), req.headers().getAll(HttpHeaders.USER_AGENT));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").send();
    }
  }

  @Nested
  class DisabledUserAgentTest extends SendTestBase {

    @Override
    void init() throws Exception {
      webClient = WebClient.wrap(client, new WebClientOptions().setUserAgentEnabled(false));
    }

    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").send();
    }
  }

  @Nested
  class OverrideUserAgentHeaderTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.singletonList("smith"), req.headers().getAll(HttpHeaders.USER_AGENT));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").putHeader(HttpHeaders.USER_AGENT.toString(), "smith").send();
    }
  }

  @Nested
  class PutHeadersTest extends SendTestBase {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    {
      headers.add("foo","bar");
      headers.add("ping","pong");
    }
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("bar", req.headers().get("foo"));
      assertEquals("pong", req.headers().get("ping"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("somehost", "somepath").putHeaders(headers).send();
    }
  }

  @Nested
  class RemoveUserAgentHeaderTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      HttpRequest<Buffer> request = client.get("somehost", "somepath");
      request.headers().remove(HttpHeaders.USER_AGENT);
      return request.send();
    }
  }

  @Nested
  class GetTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.GET, req.method());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
  }

  @Nested
  class HeadTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.HEAD, req.method());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.head(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
  }

  @Nested
  class DeleteTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.DELETE, req.method());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.delete(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
  }

  @Nested
  class QueryParamTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("param=param_value", req.query());
      assertEquals("param_value", req.getParam("param"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").addQueryParam("param", "param_value").send();
    }
  }

  @Nested
  class QueryParamMultiTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("param=param_value1&param=param_value2", req.query());
      assertEquals(Arrays.asList("param_value1", "param_value2"), req.params().getAll("param"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").addQueryParam("param", "param_value1").addQueryParam("param", "param_value2").send();
    }
  }

  @Nested
  class QueryParamAppendTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("param1=param1_value1&param1=param1_value2&param2=param2_value", req.query());
      assertEquals("param1_value1", req.getParam("param1"));
      assertEquals("param2_value", req.getParam("param2"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/?param1=param1_value1").addQueryParam("param1", "param1_value2").addQueryParam("param2", "param2_value").send();
    }
  }

  @Nested
  class OverwriteQueryParamTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("param=param_value2", req.query());
      assertEquals("param_value2", req.getParam("param"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/?param=param_value1").setQueryParam("param", "param_value2").send();
    }
  }

  @Nested
  class QueryParamEncodingTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("param1=%20&param2=%E2%82%AC", req.query());
      assertEquals(" ", req.getParam("param1"));
      assertEquals("\u20AC", req.getParam("param2"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client
        .get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/")
        .addQueryParam("param1", " ")
        .addQueryParam("param2", "\u20AC")
        .send();
    }
  }

  @Nested
  class PreserveRequestUriQueryParamTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("/remote-server?jREJBBB5x2AaiSSDO0/OskoCztDZBAAAAAADV1A4", req.uri());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("/remote-server?jREJBBB5x2AaiSSDO0/OskoCztDZBAAAAAADV1A4").send();
    }
  }

  @Nested
  class RawMethodTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.valueOf("MY_METHOD"), req.method());
      assertEquals("MY_METHOD", req.method().name());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.request(HttpMethod.valueOf("MY_METHOD"), "/").send();
    }
  }

  @Nested
  class OverwriteHeaderTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.singletonList("2"), req.headers().getAll("bla"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client
        .get("somepath")
        .putHeader("bla", "1")
        .putHeader("bla", "2")
        .send();
    }
  }

  @Nested
  class MultiValuedHeaderTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client
        .get("somepath")
        .putHeader("bla", Arrays.asList("1", "2"))
        .send();
    }
  }

  @Nested
  class ResolvingAddressTest extends SendTestBase {
    @Override
    void init() {
      client.close().await();
      FakeAddressResolver<?> resolver = new FakeAddressResolver<>();
      resolver.registerAddress("mars", Collections.singletonList(testAddress));
      client = vertx.httpClientBuilder().with(createBaseClientOptions()).withAddressResolver(resolver).build();
      webClient = WebClient.wrap(client);
    }
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("localhost:8080", req.authority().toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.request(HttpMethod.GET, new RequestOptions().setServer(new FakeAddress("mars"))).send();
    }
  }

  @Nested
  class FormUrlEncodedTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      req.setExpectMultipart(true);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals("param1_value", req.getFormAttribute("param1"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form.add("param1", "param1_value");
      return client.post("/somepath").sendForm(form);
    }
  }

  @Nested
  class FormUrlEncodedWithCharsetTest extends SendTestBase {

    final String str;
    final String expected;

    public FormUrlEncodedWithCharsetTest() throws Exception {
      str = "ø";
      expected = URLDecoder.decode(URLEncoder.encode(str, StandardCharsets.ISO_8859_1.name()), StandardCharsets.UTF_8.name());
    }

    @Override
    void requestBegin(HttpServerRequest req) {
      req.setExpectMultipart(true);
    }

    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals(expected, req.getFormAttribute("param1"));
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form.add("param1", str);
      return client.post("/somepath").sendForm(form, StandardCharsets.ISO_8859_1.name());
    }
  }

  @Nested
  class FormUrlEncodedUnescapedTest extends SendTestBase {

    @Override
    void requestBegin(HttpServerRequest req) {
      req.setExpectMultipart(true);
    }
    @Override
    void requestEnd(HttpServerRequest req, Buffer body) {
      assertEquals("grant_type=client_credentials&resource=https%3A%2F%2Fmanagement.core.windows.net%2F", body.toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form
        .set("grant_type", "client_credentials")
        .set("resource", "https://management.core.windows.net/");
      return client.post("/somepath").sendForm(form);
    }
  }

  @Nested
  class FormUrlEncodedMultiValuedHeaderTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      req.setExpectMultipart(true);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      return client.post("/somepath")
        .putHeader("bla", Arrays.asList("1", "2"))
        .sendForm(form);
    }
  }

  @Nested
  class MultipartFormDataTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertTrue(req.getHeader(HttpHeaders.CONTENT_TYPE).startsWith("multipart/form-data"));
      req.setExpectMultipart(true);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals("param1_value", req.getFormAttribute("param1"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form.add("param1", "param1_value");
      return client.post("/somepath")
        .putHeader("content-type", "multipart/form-data")
        .sendForm(form);
    }
  }

  @Nested
  class MultipartFormDataWithCharsetTest extends SendTestBase {
    @Override
    void requestEnd(HttpServerRequest req, Buffer body) {
      assertTrue(body.toString().contains("content-type: text/plain; charset=ISO-8859-1"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      MultiMap form = MultiMap.caseInsensitiveMultiMap();
      form.add("param1", "param1_value");
      return client.post("/somepath")
        .putHeader("content-type", "multipart/form-data")
        .sendForm(form, "ISO-8859-1");
    }
  }

  @Nested
  class MultipleRedirectsTest extends SendTestBase {
    final String middleRedirect = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/middle";
    final String endRedirect = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/ok";

    @Override
    void requestBegin(HttpServerRequest req) {
      if (!req.headers().contains("foo", "bar", true)) {
        fail("Missing expected header");
      } else if (req.path().equals("/redirect")) {
        req.response().setStatusCode(301).putHeader("Location", middleRedirect).end();
      } else if (req.path().equals("/middle")) {
        req.response().setStatusCode(301).putHeader("Location", endRedirect).end();
      } else {
        req.response().end(req.path());
      }
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("/redirect")
        .putHeader("foo", "bar")
        .followRedirects(true)
        .send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals("/ok", resp.body().toString());
      assertEquals(2, resp.followedRedirects().size());
      assertEquals(middleRedirect, resp.followedRedirects().get(0));
      assertEquals(endRedirect, resp.followedRedirects().get(1));
    }
  }

  @Nested
  class InvalidRedirectTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.POST, req.method());
      assertEquals("/redirect", req.path());
      req.response().setStatusCode(302).putHeader("Location", "http://www.google.com").end();
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.post("/redirect").followRedirects(true).send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(302, resp.statusCode());
      assertEquals("http://www.google.com", resp.getHeader("Location"));
      assertNull(resp.body());
    }
  }

  @Nested
  class RedirectLimitTest extends SendTestBase {

    final String location = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/redirect";

    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(HttpMethod.GET, req.method());
      assertEquals("/redirect", req.path());
      req.response().setStatusCode(302).putHeader("Location", location).end();
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client
        .get("/redirect")
        .followRedirects(true)
        .send();
    }

    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(302, resp.statusCode());
      assertEquals(location, resp.getHeader("Location"));
      assertNull(resp.body());
    }
  }

  @Nested
  class VirtualHostTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("another-host:8080", req.authority().toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("/test").virtualHost("another-host").send();
    }
  }

  @Nested
  class SocketAddressSendTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("another-host:8080", req.authority().toString());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      SocketAddress addr = SocketAddress.inetSocketAddress(8080, "localhost");
      return client.request(HttpMethod.GET, addr, 8080, "another-host", "/test").send();
    }
  }

  abstract class SendStreamTestBase extends SendTestBase {

    private final HttpMethod method;
    Integer length;
    ReadStream<Buffer> stream;

    SendStreamTestBase(HttpMethod method) {
      this.method = method;
    }

    SendStreamTestBase() {
      this(HttpMethod.POST);
    }

    @Override
    void requestEnd(HttpServerRequest req, Buffer body) {
      assertEquals(method, req.method());
      assertBody(body);
    }

    protected void assertBody(Buffer body) {
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      HttpRequest<Buffer> builder = webClient.request(method, DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
      if (length != null) {
        builder = builder.putHeader("Content-Length", "" + length);
      }
      return builder.sendStream(stream);
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
      assertEquals(200, response.statusCode());
    }
  }

  abstract class SendFileTestBase extends SendStreamTestBase {

    private final boolean chunked;
    private String expected;
    private File f;

    public SendFileTestBase(HttpMethod method, boolean chunked) {
      super(method);
      this.chunked = chunked;
    }

    @Override
    void init() throws Exception {
      expected = TestUtils.randomAlphaString(1024 * 1024);
      f = File.createTempFile("vertx", ".data");
      java.nio.file.Files.write(f.toPath(), expected.getBytes(StandardCharsets.UTF_8));
      stream = vertx.fileSystem().openBlocking(f.getAbsolutePath(), new OpenOptions());
      if (!chunked) {
        length = expected.length();
      }
    }

    @Override
    protected void assertBody(Buffer body) {
      assertEquals(Buffer.buffer(expected), body);
    }
  }

  @Nested
  public class SendFilePostTest extends SendFileTestBase {
    public SendFilePostTest() {
      super(HttpMethod.POST, false);
    }
  }

  @Nested
  public class SendFileChunkedPostTest extends SendFileTestBase {
    public SendFileChunkedPostTest() {
      super(HttpMethod.POST, true);
    }
  }

  @Nested
  public class SendFilePutTest extends SendFileTestBase {
    public SendFilePutTest() {
      super(HttpMethod.PUT, false);
    }
  }

  @Nested
  public class SendFileChunkedPutTest extends SendFileTestBase {
    public SendFileChunkedPutTest() {
      super(HttpMethod.PUT, true);
    }
  }

  @Nested
  public class SendFilePatchTest extends SendFileTestBase {
    public SendFilePatchTest() {
      super(HttpMethod.PATCH, false);
    }
  }

  @Nested
  class RequestConnectErrorTest extends SendStreamTestBase {

    AtomicInteger closed = new AtomicInteger();

    @Override
    protected void bind() {
      // No bind
    }

    @Override
    void init() throws Exception {
      stream = new ReadStream<>() {
        @Override
        public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> handler) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> pause() {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> resume() {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> fetch(long amount) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
          throw new UnsupportedOperationException();
        }
        @Override
        public Pipe<Buffer> pipe() {
          return new Pipe<>() {
            @Override
            public Pipe<Buffer> endOnFailure(boolean end) {
              throw new UnsupportedOperationException();
            }
            @Override
            public Pipe<Buffer> endOnSuccess(boolean end) {
              throw new UnsupportedOperationException();
            }
            @Override
            public Pipe<Buffer> endOnComplete(boolean end) {
              throw new UnsupportedOperationException();
            }
            @Override
            public Future<Void> to(WriteStream<Buffer> dst) {
              throw new UnsupportedOperationException();
            }
            @Override
            public void close() {
              closed.incrementAndGet();
            }
          };
        }
      };
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(Exception.class);
      Assert.assertEquals(1, closed.get());
    }
  }

  @Nested
  class SendStreamErrorTest extends SendStreamTestBase {

    AtomicReference<HttpConnection> conn = new AtomicReference<>();
    AtomicReference<Handler<Buffer>> dataHandler = new AtomicReference<>();
    AtomicReference<Handler<Void>> endHandler = new AtomicReference<>();
    AtomicBoolean paused = new AtomicBoolean();
    Promise<Void> requestReceived = Promise.promise();

    @Override
    void init() throws Exception {
      stream = new ReadStream<>() {
        @Override
        public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
          return this;
        }
        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> handler) {
          dataHandler.set(handler);
          return this;
        }
        @Override
        public ReadStream<Buffer> pause() {
          paused.set(true);
          return this;
        }
        @Override
        public ReadStream<Buffer> fetch(long amount) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ReadStream<Buffer> resume() {
          paused.set(false);
          return this;
        }
        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> handler) {
          endHandler.set(handler);
          return this;
        }
      };
    }

    @Override
    void handleRequest(HttpServerRequest req) {
      conn.set(req.connection());
      req.pause();
      requestReceived.complete();
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      Future<HttpResponse<Buffer>> fut = super.send(client);
      assertWaitUntil(() -> dataHandler.get() != null);
      dataHandler.get().handle(TestUtils.randomBuffer(1024));
      try {
        requestReceived.future().await();
      } catch (Exception e) {
        fail(e);
      }
      while (!paused.get()) {
        dataHandler.get().handle(TestUtils.randomBuffer(1024));
      }
      conn.get().close();
      return fut;
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(Exception.class);
    }
  }

  @Nested
  class RequestPumpErrorTest extends SendStreamTestBase {

    final Throwable cause = new Throwable();
    CompletableFuture<Void> done = new CompletableFuture<>();

    @Override
    void init() throws Exception {
      stream = new ReadStream<>() {
        @Override
        public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
          if (handler != null) {
            done.thenAccept(v -> handler.handle(cause));
          }
          return this;
        }
        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> handler) {
          if (handler != null) {
            handler.handle(TestUtils.randomBuffer(1024));
          }
          return this;
        }
        @Override
        public ReadStream<Buffer> fetch(long amount) {
          return this;
        }
        @Override
        public ReadStream<Buffer> pause() {
          return this;
        }
        @Override
        public ReadStream<Buffer> resume() {
          return this;
        }
        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
          return this;
        }
      };
    }

    @Override
    void requestBegin(HttpServerRequest req) {
      req.handler(chunk -> {
        done.complete(null);
        req.handler(null);
      });
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      assertSame(cause, failure);
    }
  }

  public abstract class SendBodyTestBase extends SendTestBase {

    String expectedContentType;
    Buffer expectedBody;

    @Override
    void requestEnd(HttpServerRequest req, Buffer body) {
      if (expectedContentType != null) {
        assertEquals(expectedContentType, req.getHeader("content-type"));
      }
      if (expectedBody != null) {
        assertEquals(expectedBody, body);
      }
    }
  }

  @Nested
  public class SendJsonObjectTest extends SendBodyTestBase {

    private final JsonObject body;

    public SendJsonObjectTest() {
      body = new JsonObject().put("wine", "Chateauneuf Du Pape").put("cheese", "roquefort");
      expectedContentType = "application/json";
      expectedBody = body.toBuffer();
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").sendJsonObject(body);
    }
  }

  @Nested
  public class SendJsonArrayTest extends SendBodyTestBase {

    private final JsonArray body;

    public SendJsonArrayTest() {
      body = new JsonArray().add(0).add(1).add(2);
      expectedContentType = "application/json";
      expectedBody = body.toBuffer();
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").sendJson(body);
    }
  }

  @Nested
  public class SendJsonNullTest extends SendBodyTestBase {

    public SendJsonNullTest() {
      expectedContentType = "application/json";
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").sendJson(null);
    }
  }

  @Nested
  public class SendBufferTest extends SendBodyTestBase {

    private final Buffer body;

    public SendBufferTest() {
      body = TestUtils.randomBuffer(2048);
      expectedBody = body;
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").sendBuffer(body);
    }
  }

  @Nested
  class SendJsonWithUserAgentDisabledTest extends SendTestBase {
    WebClient agentFreeClient;
    @Override
    void init() {
      WebClientOptions clientOptions = new WebClientOptions()
        .setDefaultHost(DEFAULT_HTTP_HOST)
        .setDefaultPort(DEFAULT_HTTP_PORT)
        .setUserAgentEnabled(false);
      agentFreeClient = WebClient.create(vertx, clientOptions);
    }
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return agentFreeClient.post("somehost", "somepath")
        .sendJson(new JsonObject().put("meaning", 42));
    }
  }

  @Nested
  class ResponseBodyAsBufferTest extends SendTestBase {
    Buffer expected = TestUtils.randomBuffer(2000);
    @Override
    void requestBegin(HttpServerRequest req) {
      req.response().end(expected);
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.body());
    }
  }

  @Nested
  class ResponseUnknownContentTypeBodyAsJsonObjectTest extends SendTestBase {
    JsonObject expected = new JsonObject().put("cheese", "Goat Cheese").put("wine", "Condrieu");
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end(expected.encode());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.bodyAsJsonObject());
    }
  }

  @Nested
  class ResponseUnknownContentTypeBodyAsJsonArrayTest extends SendTestBase {
    JsonArray expected = new JsonArray().add("cheese").add("wine");
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end(expected.encode());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.bodyAsJsonArray());
    }
  }

  @Nested
  class ResponseNullContentTypeBodyAsTest extends SendTestBase {
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end("null");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.bodyAsJsonObject());
      assertNull(resp.bodyAsJsonArray());
      assertNull(resp.bodyAsJson(WineAndCheese.class));
    }
  }

  @Nested
  class QueryParamSpecialCharsTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("c%3Ad=e", req.query());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("/test").addQueryParam("c:d", "e").send();
    }
  }

  @Nested
  class QueryParamSpecialCharsDirectTest extends SendTestBase {
    @Override
    void requestBegin(HttpServerRequest req) {
      assertEquals("c:d=e", req.query());
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get("/test/?c:d=e").send();
    }
  }

  @Nested
  class ResponseInvalidContentTypeBodyAsTest extends SendTestBase {
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end("\"1234");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      Assertions.assertThatThrownBy(() -> resp.bodyAsJsonObject()).isInstanceOf(DecodeException.class);
      Assertions.assertThatThrownBy(() -> resp.bodyAsJsonArray()).isInstanceOf(DecodeException.class);
      Assertions.assertThatThrownBy(() -> resp.bodyAsJson(WineAndCheese.class)).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class ResponseAnotherContentTypeBodyAsTest extends SendTestBase {
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end("1234");
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").send();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      Assertions.assertThatThrownBy(() -> resp.bodyAsJsonObject()).isInstanceOf(DecodeException.class);
      Assertions.assertThatThrownBy(() -> resp.bodyAsJsonArray()).isInstanceOf(DecodeException.class);
      Assertions.assertThatThrownBy(() -> resp.bodyAsJson(WineAndCheese.class)).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class SendHttpServerRequestStreamTest extends SendTestBase {

    Buffer expected = TestUtils.randomBuffer(10000);

    @Override
    void init() {
      HttpServer backend = vertx
        .createHttpServer(new HttpServerOptions().setPort(8081))
        .requestHandler(req -> req.bodyHandler(body -> {
          assertEquals(body, expected);
          req.response().end();
        }));
      startServer(backend);
    }

    @Override
    void handleRequest(HttpServerRequest req) {
      webClient.postAbs("http://localhost:8081/")
        .sendStream(req)
        .onComplete(TestUtils.onSuccess(resp -> req.response().end("ok")));
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return webClient
        .post(8080, "localhost", "/")
        .sendBuffer(expected);
    }
  }

  abstract class ProxyTestBase extends SendTestBase {

    ProxyBase<?> proxy;

    @Override
    void init() throws Exception {
      proxy = new HttpProxy();
      proxy.setForceUri("http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT);
      proxy.start(vertx);
    }

    @AfterEach
    public void after() {
      ProxyBase<?> p = proxy;
      if (p != null) {
        proxy = null;
        p.stop();
      }
    }
  }

  @Nested
  class HttpRequestProxyTest extends ProxyTestBase {

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return webClient
        .get("http://checkip.amazonaws.com/")
        .proxy(new ProxyOptions().setPort(proxy.port()))
        .send();
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
      assertEquals(200, response.statusCode());
      assertEquals("http://checkip.amazonaws.com/", proxy.getLastUri());
    }
  }

  @Nested
  class FtpRequestProxyTest extends ProxyTestBase {

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return webClient
        .get("ftp://ftp.gnu.org/gnu/")
        .proxy(new ProxyOptions().setPort(proxy.port()))
        .send();
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
      assertEquals(200, response.statusCode());
      assertEquals("ftp://ftp.gnu.org/gnu/", proxy.getLastUri());
    }
  }

  @Nested
  class ConnectErrorTest extends SendTestBase {
    @Override
    protected void bind() {
      // No bind
    }
    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return client
        .get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath")
        .send();
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(ConnectException.class);
    }
  }

  @Nested
  class SendTimeoutTest extends SendTestBase {
    @Override
    void handleRequest(HttpServerRequest req) {
    }
    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath")
        .timeout(50)
        .send();
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(TimeoutException.class);
    }
  }

  @Nested
  class TimeoutRequestBeforeSendingTest extends SendTimeoutTest {
    @Override
    void handleRequest(HttpServerRequest req) {
      // Ignore
    }

    @RepeatedTest(100)
    @Override
    void run() throws Exception {
      super.run();
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return client
        .get(8080, "localhost", "/")
        .timeout(1)
        .send();
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      super.assertResponseFailure(failure);
    }
  }

  class TLSTestBase extends SendTestBase {
    WebClientOptions clientOptions;
    HttpServerOptions serverOptions;
    Consumer<HttpServerRequest> serverAssertions;
    Function<WebClient, HttpRequest<Buffer>> requestProvider;
    TLSTestBase() {
    }
    TLSTestBase(boolean clientSSL, boolean serverSSL) {
      clientOptions = new WebClientOptions()
        .setSsl(clientSSL)
        .setTrustAll(true)
        .setDefaultHost(DEFAULT_HTTPS_HOST)
        .setDefaultPort(DEFAULT_HTTPS_PORT);
      serverOptions = new HttpServerOptions()
        .setSsl(serverSSL)
        .setKeyCertOptions(Cert.SERVER_JKS.get())
        .setPort(DEFAULT_HTTPS_PORT)
        .setHost(DEFAULT_HTTPS_HOST);
    }
    @Override
    void init() throws Exception {
      webClient = WebClient.create(vertx, clientOptions);
      server = vertx.createHttpServer(serverOptions);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals(serverOptions.isSsl(), req.isSSL());
      if (serverAssertions != null) {
        serverAssertions.accept(req);
      }
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      return requestProvider
        .apply(client)
        .send();
    }
  }

  @Nested
  class TLSEnabledTest extends TLSTestBase {
    TLSEnabledTest() {
      super(true, true);
      requestProvider = client -> client.get("/");
    }
  }

  @Nested
  class TLSEnabledDisableRequestTLSTest extends TLSTestBase {
    TLSEnabledDisableRequestTLSTest() {
      super(true, false);
      requestProvider = client -> client.get("/").ssl(false);
    }
  }

  @Nested
  class TLSEnabledEnableRequestTLSTest extends TLSTestBase {
    TLSEnabledEnableRequestTLSTest() {
      super(true, true);
      requestProvider = client -> client.get("/").ssl(true);
    }
  }

  @Nested
  class TLSDisabledDisableRequestTLSTest extends TLSTestBase {
    TLSDisabledDisableRequestTLSTest() {
      super(false, false);
      requestProvider = client -> client.get("/").ssl(false);
    }
  }

  @Nested
  class TLSDisabledEnableRequestTLSTest extends TLSTestBase {
    TLSDisabledEnableRequestTLSTest() {
      super(false, true);
      requestProvider = client -> client.get("/").ssl(true);
    }
  }

  @Nested
  class TLSEnabledDisableRequestTLSAbsURITest extends TLSTestBase {
    TLSEnabledDisableRequestTLSAbsURITest() {
      super(true, false);
      requestProvider = client -> client.getAbs("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT);
    }
  }

  @Nested
  class TLSEnabledEnableRequestTLSAbsURITest extends TLSTestBase {
    TLSEnabledEnableRequestTLSAbsURITest() {
      super(true, true);
      requestProvider = client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT);
    }
  }

  @Nested
  class TLSDisabledDisableRequestTLSAbsURITest extends TLSTestBase {
    TLSDisabledDisableRequestTLSAbsURITest() {
      super(false, false);
      requestProvider = client -> client.getAbs("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT);
    }
  }

  @Nested
  class TLSDisabledEnableRequestTLSAbsURITest extends TLSTestBase {
    TLSDisabledEnableRequestTLSAbsURITest() {
      super(false, true);
      requestProvider = client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT);
    }
  }

  @Nested
  class TLSEnabledDisableRequestTLSAbsURIWithOptionsTest extends TLSTestBase {
    TLSEnabledDisableRequestTLSAbsURIWithOptionsTest() {
      super(true, false);
      requestProvider = client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
    }
  }

  @Nested
  class TLSEnabledEnableRequestTLSAbsURIWithOptionsTest extends TLSTestBase {
    TLSEnabledEnableRequestTLSAbsURIWithOptionsTest() {
      super(true, true);
      requestProvider = client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
    }
  }

  @Nested
  class TLSDisabledDisableRequestTLSAbsURIWithOptionsTest extends TLSTestBase {
    TLSDisabledDisableRequestTLSAbsURIWithOptionsTest() {
      super(false, false);
      requestProvider = client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
    }
  }

  @Nested
  class TLSDisabledEnableRequestTLSAbsURIWithOptionsTest extends TLSTestBase {
    TLSDisabledEnableRequestTLSAbsURIWithOptionsTest() {
      super(false, true);
      requestProvider = client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
    }
  }

  @Nested
  class TLSQueryParametersIssue563Test extends TLSTestBase {
    TLSQueryParametersIssue563Test() {
      super(false, true);
      requestProvider = client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)
        .addQueryParam("query1", "value1")
        .addQueryParam("query2", "value2");
      serverAssertions = req -> assertEquals("query1=value1&query2=value2", req.query());
    }
  }

  @Nested
  class VirtualHostSNITest extends TLSTestBase {
    VirtualHostSNITest() {
      clientOptions = new WebClientOptions()
        .setTrustAll(true)
        .setDefaultHost(DEFAULT_HTTPS_HOST)
        .setDefaultPort(DEFAULT_HTTPS_PORT);
      serverOptions = new HttpServerOptions()
        .setSsl(true)
        .setSni(true)
        .setKeyCertOptions(Cert.SNI_JKS.get())
        .setPort(DEFAULT_HTTPS_PORT)
        .setHost(DEFAULT_HTTPS_HOST);
      requestProvider = client -> client.get("/").virtualHost("host2.com").ssl(true);
      serverAssertions = req -> assertEquals("host2.com", req.connection().indicatedServerName());
    }
  }

  abstract class MultipartFormTestBase extends SendTestBase {

    MultipartForm form = MultipartForm.create();
    boolean multipartMixed = true;

    @Override
    void requestBegin(HttpServerRequest req) {
      req.setExpectMultipart(true);
    }

    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      HttpRequest<Buffer> builder = webClient.post("somepath");
      builder.multipartMixed(multipartMixed);
      return builder.sendMultipartForm(form);
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
    }
  }

  @Nested
  class MultipartFormHeadersTest extends MultipartFormTestBase {
    @Override
    void requestEnd(HttpServerRequest req) {
      assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla"));
    }
    @Override
    Future<HttpResponse<Buffer>> send(WebClient client) {
      HttpRequest<Buffer> builder = webClient.post("somepath");
      builder.putHeader("bla", Arrays.asList("1", "2"));
      builder.multipartMixed(multipartMixed);
      return builder.sendMultipartForm(form);
    }
  }

  @Nested
  class MultipartFormUploadNonExistingFileTest extends MultipartFormTestBase {

    public MultipartFormUploadNonExistingFileTest() {
      form.textFileUpload("file", "nonexistentFilename", "nonexistentPathname", "text/plain");
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure)
        .rootCause()
        .isInstanceOf(FileNotFoundException.class);
    }
  }

  abstract class MultipartFormUploadTestBase extends MultipartFormTestBase {

    List<Upload> uploads = Collections.synchronizedList(new ArrayList<>());
    List<Upload> toUpload = new ArrayList<>();

    void assertUploads(List<Upload> uploads) {
    }

    @Override
    void requestBegin(HttpServerRequest req) {
      super.requestBegin(req);
      req.uploadHandler(upload -> {
        Buffer fileBuffer = Buffer.buffer();
        assertEquals("text/plain", upload.contentType());
        upload.handler(fileBuffer::appendBuffer);
        upload.endHandler(v -> {
          uploads.add(new Upload(upload.name(), upload.filename(), true, upload.charset(), fileBuffer));
        });
      });
    }

    @Override
    void init() throws Exception {
      List<Upload> toUpload = this.toUpload;
      File[] testFiles = new File[toUpload.size()];
      for (int i = 0;i < testFiles.length;i++) {
        Upload upload = toUpload.get(i);
        if (upload.file) {
          String name = upload.filename;
          testFiles[i] = new File(testFolder, name);
          vertx.fileSystem().writeFileBlocking(testFiles[i].getPath(), upload.data);
          form.textFileUpload(toUpload.get(i).name, toUpload.get(i).filename, testFiles[i].getPath(), "text/plain");
        } else {
          form.textFileUpload(toUpload.get(i).name, toUpload.get(i).filename, upload.data, "text/plain");
        }
      }
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
      assertUploads(uploads);
    }
  }

  @Nested
  class MultipartFormUpload32BTest extends MultipartFormUploadTestBase {
    final Buffer content;
    MultipartFormUpload32BTest() {
      content = Buffer.buffer(TestUtils.randomAlphaString(32));
      form.attribute("toolkit", "vert.x").attribute("runtime", "jvm");
      toUpload.add(Upload.fileUpload("test", "test.txt", content));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(1, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test.txt", uploads.get(0).filename);
      assertEquals(content, uploads.get(0).data);
    }
  }

  @Nested
  class MultipartFormUpload32KTest extends MultipartFormUploadTestBase {
    final Buffer content;
    MultipartFormUpload32KTest() {
      content = Buffer.buffer(TestUtils.randomAlphaString(32 * 1024));
      form.attribute("toolkit", "vert.x").attribute("runtime", "jvm");
      toUpload.add(Upload.fileUpload("test", "test.txt", content));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(1, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test.txt", uploads.get(0).filename);
      assertEquals(content, uploads.get(0).data);
    }
  }

  @Nested
  class MultipartFormUpload32MTest extends MultipartFormUploadTestBase {
    final Buffer content;
    MultipartFormUpload32MTest() {
      content = Buffer.buffer(TestUtils.randomAlphaString(32 * 1024 * 1024));
      form.attribute("toolkit", "vert.x").attribute("runtime", "jvm");
      toUpload.add(Upload.fileUpload("test", "test.txt", content));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(1, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test.txt", uploads.get(0).filename);
      assertEquals(content, uploads.get(0).data);
    }
  }

  @Nested
  class MultipartFormUploadFromHeapTest extends MultipartFormUploadTestBase {
    final Buffer content;
    MultipartFormUploadFromHeapTest() {
      content = Buffer.buffer(TestUtils.randomAlphaString(32 * 1024));
      form.attribute("toolkit", "vert.x").attribute("runtime", "jvm");
      toUpload.add(Upload.memoryUpload("test", "test.txt", content));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(1, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test.txt", uploads.get(0).filename);
      assertEquals(content, uploads.get(0).data);
    }
  }

  @Nested
  class MultipartFormUploadsTest extends MultipartFormUploadTestBase {
    final Buffer content1;
    final Buffer content2;
    MultipartFormUploadsTest() {
      content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
      content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
      toUpload.add(Upload.fileUpload("test1", "test1.txt", content1));
      toUpload.add(Upload.fileUpload("test2", "test2.txt", content2));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(2, uploads.size());
      assertEquals("test1", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals("UTF-8", uploads.get(0).charset);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test2", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals("UTF-8", uploads.get(1).charset);
      assertEquals(content2, uploads.get(1).data);
    }
  }

  @Nested
  class MultipartFormUploadWithCharsetTest extends MultipartFormUploadTestBase {
    final Buffer content;
    MultipartFormUploadWithCharsetTest() {
      content = Buffer.buffer(TestUtils.randomAlphaString(16));
      form.setCharset(StandardCharsets.ISO_8859_1);
      toUpload.add(Upload.fileUpload("test1", "test1.txt", content));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(1, uploads.size());
      assertEquals("test1", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals("ISO-8859-1", uploads.get(0).charset);
    }
  }

  @Nested
  class MultipartFormUploadSameFileNameTest extends MultipartFormUploadTestBase {
    final Buffer content1;
    final Buffer content2;
    MultipartFormUploadSameFileNameTest() {
      content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
      content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
      toUpload.add(Upload.fileUpload("test", "test1.txt", content1));
      toUpload.add(Upload.fileUpload("test", "test2.txt", content2));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(2, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals(content2, uploads.get(1).data);
    }
  }

  @Nested
  class MultipartFormUploadDisableMixedTest extends MultipartFormUploadTestBase {
    final Buffer content1;
    final Buffer content2;
    MultipartFormUploadDisableMixedTest() {
      content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
      content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
      multipartMixed = false;
      toUpload.add(Upload.fileUpload("test", "test1.txt", content1));
      toUpload.add(Upload.fileUpload("test", "test2.txt", content2));
    }
    @Override
    void assertUploads(List<Upload> uploads) {
      assertEquals(2, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals(content2, uploads.get(1).data);
    }
  }

  static class Upload {
    final String name;
    final String filename;
    final String charset;
    final Buffer data;
    final boolean file;
    private Upload(String name, String filename, boolean file, String charset, Buffer data) {
      this.name = name;
      this.filename = filename;
      this.charset = charset;
      this.data = data;
      this.file = file;
    }
    static Upload fileUpload(String name, String filename, Buffer data) {
      return new Upload(name, filename, true, null, data);
    }
    static Upload memoryUpload(String name, String filename, Buffer data) {
      return new Upload(name, filename, false, null, data);
    }
  }

  class BodyCodecTestBase<T> extends SendTestBase {

    BodyCodec<T> bodyCodec;
    String body;

    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end(body);
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath").as(bodyCodec).send();
    }
  }

  @Nested
  class NullLiteralResponseBodyAsJsonObjectTest extends BodyCodecTestBase<JsonObject> {
    NullLiteralResponseBodyAsJsonObjectTest() {
      bodyCodec = BodyCodec.jsonObject();
      body = "null";
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class AnotherJsonResponseBodyAsJsonObjectTest extends BodyCodecTestBase<JsonObject> {
    AnotherJsonResponseBodyAsJsonObjectTest() {
      bodyCodec = BodyCodec.jsonObject();
      body = "1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class InvalidJsonResponseBodyAsJsonObjectTest extends BodyCodecTestBase<JsonObject> {
    InvalidJsonResponseBodyAsJsonObjectTest() {
      bodyCodec = BodyCodec.jsonObject();
      body = "\"1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class NullLiteralResponseBodyAsJsonMappedTest extends BodyCodecTestBase<WineAndCheese> {
    NullLiteralResponseBodyAsJsonMappedTest() {
      bodyCodec = BodyCodec.json(WineAndCheese.class);
      body = "null";
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class AnotherJsonResponseBodyAsJsonMappedTest extends BodyCodecTestBase<WineAndCheese> {
    AnotherJsonResponseBodyAsJsonMappedTest() {
      bodyCodec = BodyCodec.json(WineAndCheese.class);
      body = "1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class InvalidJsonResponseBodyAsJsonMappedTest extends BodyCodecTestBase<WineAndCheese> {
    InvalidJsonResponseBodyAsJsonMappedTest() {
      bodyCodec = BodyCodec.json(WineAndCheese.class);
      body = "\"1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class NullLiteralResponseBodyAsJsonArrayTest extends BodyCodecTestBase<JsonArray> {
    NullLiteralResponseBodyAsJsonArrayTest() {
      bodyCodec = BodyCodec.jsonArray();
      body = "null";
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class AnotherJsonResponseBodyAsJsonArrayTest extends BodyCodecTestBase<JsonArray> {
    AnotherJsonResponseBodyAsJsonArrayTest() {
      bodyCodec = BodyCodec.jsonArray();
      body = "1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions
        .assertThat(failure)
        .isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class InvalidJsonResponseBodyAsJsonArrayTest extends BodyCodecTestBase<JsonArray> {
    InvalidJsonResponseBodyAsJsonArrayTest() {
      bodyCodec = BodyCodec.jsonArray();
      body = "\"1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class NullLiteralResponseBodyAsJsonArrayMappedTest extends BodyCodecTestBase<List> {
    NullLiteralResponseBodyAsJsonArrayMappedTest() {
      bodyCodec = BodyCodec.json(List.class);
      body = "null";
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class AnotherJsonResponseBodyAsJsonArrayMappedTest extends BodyCodecTestBase<List> {
    AnotherJsonResponseBodyAsJsonArrayMappedTest() {
      bodyCodec = BodyCodec.json(List.class);
      body = "1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class InvalidJsonResponseBodyAsJsonArrayMappedTest extends BodyCodecTestBase<List> {
    InvalidJsonResponseBodyAsJsonArrayMappedTest() {
      bodyCodec = BodyCodec.json(List.class);
      body = "\"1234";
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(DecodeException.class);
    }
  }

  @Nested
  class ResponseBodyDiscardedTest extends BodyCodecTestBase<Void> {
    ResponseBodyDiscardedTest() {
      bodyCodec = BodyCodec.none();
      body = TestUtils.randomAlphaString(1024);
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class ResponseBodyAsJsonArrayTest extends BodyCodecTestBase<JsonArray> {
    final JsonArray expected;
    ResponseBodyAsJsonArrayTest() {
      expected = new JsonArray().add("cheese").add("wine");
      bodyCodec = BodyCodec.jsonArray();
      body = expected.encode();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.body());
    }
  }

  @Nested
  class ResponseBodyAsJsonArrayMappedTest extends BodyCodecTestBase<List> {
    final JsonArray expected;
    ResponseBodyAsJsonArrayMappedTest() {
      expected = new JsonArray().add("cheese").add("wine");
      bodyCodec = BodyCodec.json(List.class);
      body = expected.encode();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(200, resp.statusCode());
      assertEquals(expected.getList(), resp.body());
    }
  }

  @Nested
  class ResponseJsonObjectMissingBodyTest extends BodyCodecTestBase<JsonObject> {
    ResponseJsonObjectMissingBodyTest() {
      bodyCodec = BodyCodec.jsonObject();
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().setStatusCode(403).end();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(403, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class ResponseJsonMissingBodyTest extends BodyCodecTestBase<WineAndCheese> {
    ResponseJsonMissingBodyTest() {
      bodyCodec = BodyCodec.json(WineAndCheese.class);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().setStatusCode(403).end();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(403, resp.statusCode());
      assertNull(resp.body());
    }
  }

  @Nested
  class ResponseWriteStreamMissingBodyTest extends BodyCodecTestBase<Void> {
    final AtomicInteger length = new AtomicInteger();
    final AtomicBoolean ended = new AtomicBoolean();
    ResponseWriteStreamMissingBodyTest() {
      WriteStream<Buffer> stream = new WriteStreamBase() {
        @Override
        public Future<Void> write(Buffer data) {
          length.addAndGet(data.length());
          return super.write(data);
        }
        @Override
        public Future<Void> end() {
          ended.set(true);
          return super.end();
        }
      };
      bodyCodec = BodyCodec.pipe(stream);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().setStatusCode(403).end();
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(403, resp.statusCode());
      assertNull(resp.body());
      assertTrue(ended.get());
      assertEquals(0, length.get());
    }
  }

  @Nested
  class ResponseBodyCodecErrorTest extends BodyCodecTestBase<Void> {
    final RuntimeException cause = new RuntimeException();
    ResponseBodyCodecErrorTest() {
      WriteStream<Buffer> stream = new WriteStreamBase() {
        Handler<Throwable> exceptionHandler;
        @Override
        public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
          exceptionHandler = handler;
          return this;
        }
        @Override
        public Future<Void> write(Buffer data) {
          exceptionHandler.handle(cause);
          return Future.failedFuture(cause);
        }
      };
      bodyCodec = BodyCodec.pipe(stream);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().setChunked(true);
      req.response().end(TestUtils.randomBuffer(2048));
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isSameAs(cause);
    }
  }

  @Nested
  class AsyncFileResponseBodyStreamTest extends BodyCodecTestBase<Void> {
    final AtomicLong received = new AtomicLong();
    final AtomicBoolean closed = new AtomicBoolean();
    AsyncFileResponseBodyStreamTest() {
      AsyncFile file = new AsyncFile() {
        public Future<Void> write(Buffer buffer, long position) { throw new UnsupportedOperationException(); }
        public Future<Buffer> read(Buffer buffer, int offset, long position, int length) { throw new UnsupportedOperationException(); }
        public AsyncFile handler(Handler<Buffer> handler) { throw new UnsupportedOperationException(); }
        public AsyncFile pause() { throw new UnsupportedOperationException(); }
        public AsyncFile resume() { throw new UnsupportedOperationException(); }
        public AsyncFile endHandler(Handler<Void> handler) { throw new UnsupportedOperationException(); }
        public AsyncFile setWriteQueueMaxSize(int i) { throw new UnsupportedOperationException(); }
        public AsyncFile drainHandler(Handler<Void> handler) { throw new UnsupportedOperationException(); }
        public AsyncFile fetch(long l) { throw new UnsupportedOperationException(); }
        public Future<Void> end() { return close(); }
        public void end(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
        public void close(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
        public void write(Buffer buffer, long l, Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
        public AsyncFile read(Buffer buffer, int i, long l, int i1, Handler<AsyncResult<Buffer>> handler) { throw new UnsupportedOperationException(); }
        public Future<Void> flush() { throw new UnsupportedOperationException(); }
        public AsyncFile flush(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
        public AsyncFile setReadPos(long l) { throw new UnsupportedOperationException(); }
        public AsyncFile setReadLength(long l) { throw new UnsupportedOperationException(); }
        public long getReadLength() { throw new UnsupportedOperationException(); }
        public AsyncFile setWritePos(long l) { throw new UnsupportedOperationException(); }
        public AsyncFile setReadBufferSize(int i) { throw new UnsupportedOperationException(); }
        public long sizeBlocking() { return 0; }
        public Future<Long> size() { return Future.succeededFuture(0L); }
        public AsyncFile exceptionHandler(Handler<Throwable> handler) { return this; }
        public boolean writeQueueFull() { return false; }
        public long getWritePos() { throw new UnsupportedOperationException(); }
        public AsyncFileLock tryLock() { throw new UnsupportedOperationException(); }
        public AsyncFileLock tryLock(long l, long l1, boolean b) { throw new UnsupportedOperationException(); }
        public Future<AsyncFileLock> lock() { throw new UnsupportedOperationException(); }
        public void lock(Handler<AsyncResult<AsyncFileLock>> handler) { throw new UnsupportedOperationException(); }
        public Future<AsyncFileLock> lock(long l, long l1, boolean b) { throw new UnsupportedOperationException(); }
        public void lock(long l, long l1, boolean b, Handler<AsyncResult<AsyncFileLock>> handler) { throw new UnsupportedOperationException(); }
        public Future<Void> write(Buffer buffer) {
          Promise<Void> promise = Promise.promise();
          write(buffer, promise);
          return promise.future();
        }
        public void write(Buffer buffer, Completable<Void> handler) {
          received.addAndGet(buffer.length());
          if (handler != null) {
            handler.succeed();
          }
        }
        public Future<Void> close() {
          return Future.future(p -> {
            vertx.setTimer(500, id -> {
              closed.set(true);
              p.complete();
            });
          });
        }
      };
      bodyCodec = BodyCodec.pipe(file);
    }
    @Override
    void requestEnd(HttpServerRequest req) {
      req.response().end(TestUtils.randomBuffer(1024 * 1024));
    }
    @Override
    void assertResponse(HttpResponse<?> resp) {
      assertEquals(1024 * 1024, received.get());
      assertTrue(closed.get());
    }
  }

  @Nested
  class CloseConnectionBodyCodecTest extends BodyCodecTestBase<JsonObject> {
    CloseConnectionBodyCodecTest() {
      bodyCodec = BodyCodec.jsonObject();
    }
    @Override
    void handleRequest(HttpServerRequest req) {
      HttpServerResponse resp = req.response();
      resp.setChunked(true)
        .write(Buffer.buffer("some-data"))
        .onComplete(ar -> req.connection().close());
    }
    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(VertxException.class);
    }
  }

  @Nested
  class ResponseBodyStreamErrorTest extends BodyCodecTestBase<Void> {

    volatile HttpServerRequest request;

    @Override
    void init() {
      AtomicInteger received = new AtomicInteger();
      WriteStream<Buffer> stream = new WriteStreamBase() {
        @Override
        public Future<Void> write(Buffer data) {
          if (received.addAndGet(data.length()) == 2048) {
            request
              .response()
              .reset();
          }
          return super.write(data);
        }
      };
      bodyCodec = BodyCodec.pipe(stream);
    }

    @Override
    void requestEnd(HttpServerRequest req) {
      request = req;
      HttpServerResponse response = req.response();
      response.setChunked(true);
      response.write(TestUtils.randomBuffer(2048));
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      Assertions.assertThat(failure).isInstanceOf(Exception.class);
    }
  }

  @Nested
  class ResponseBodyCodecErrorBeforeResponseIsReceivedTest extends BodyCodecTestBase<Void> {

    RuntimeException cause;
    WriteStreamBase stream;

    @Override
    void init() throws Exception {
      cause = new RuntimeException();
      stream = new WriteStreamBase() {
      };
      bodyCodec = BodyCodec.pipe(stream);
    }

    @Override
    void requestEnd(HttpServerRequest req) {
      HttpServerResponse resp = req.response();
      resp.setChunked(true);
      resp.end(TestUtils.randomBuffer(2048));
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      Future<? extends HttpResponse<?>> fut = super.send(client);
      assertNotNull(stream.exceptionHandler);
      stream.exceptionHandler.handle(cause);
      return fut;
    }

    @Override
    void assertResponseFailure(Throwable failure) {
      assertSame(cause, failure);
    }
  }

  class ResponseBodyStreamTestBase extends SendTestBase {

    final Promise<Void> resume = Promise.promise();
    final AtomicBoolean ended = new AtomicBoolean();
    final boolean close;

    public ResponseBodyStreamTestBase(boolean close) {
      this.close = close;
    }

    @Override
    void requestEnd(HttpServerRequest req) {
      HttpServerResponse response = req.response();
      response.setChunked(true);
      vertx.setPeriodic(1, id -> {
        if (!response.writeQueueFull()) {
          response.write(TestUtils.randomAlphaString(1024));
        } else {
          response.drainHandler(v -> response.end());
          vertx.cancelTimer(id);
          resume.succeed();
        }
      });
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      AtomicInteger size = new AtomicInteger();
      WriteStream<Buffer> stream = new WriteStreamBase() {
        boolean paused = true;
        {
          resume.future().onSuccess(v -> {
            paused = false;
            if (drainHandler != null) {
              drainHandler.handle(null);
            }
          });
        }
        @Override
        public Future<Void> write(Buffer data) {
          size.addAndGet(data.length());
          return super.write(data);
        }
        @Override
        public Future<Void> end() {
          ended.set(true);
          return super.end();
        }

        @Override
        public boolean writeQueueFull() {
          return paused;
        }
      };
      return webClient
        .get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath")
        .as(BodyCodec.pipe(stream, close)).send();
    }
    @Override
    void assertResponse(HttpResponse<?> response) {
      assertEquals(close, ended.get());
      assertEquals(200, response.statusCode());
      assertNull(response.body());
    }
  }

  @Nested
  class ResponseBodyStreamTest extends ResponseBodyStreamTestBase {
    public ResponseBodyStreamTest() {
      super(true);
    }
  }

  @Nested
  class ResponseBodyStreamNoCloseTest extends ResponseBodyStreamTestBase {
    public ResponseBodyStreamNoCloseTest() {
      super(false);
    }
  }

  class FollowRedirectsTestBase extends SendTestBase {

    private static final String location = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/ok";

    final Boolean set;
    final boolean expect;

    FollowRedirectsTestBase(Boolean set, boolean expect) {
      this.set = set;
      this.expect = expect;
    }

    @Override
    void requestEnd(HttpServerRequest req) {
      if (!req.headers().contains("foo", "bar", true)) {
        fail("Missing expected header");
      } else {
        assertEquals(Collections.singletonList("bar"), req.headers().getAll("foo"));
        if (req.path().equals("/redirect")) {
          req.response().setStatusCode(301).putHeader("Location", location).end();
        } else {
          req.response().end(req.path());
        }
      }
    }

    @Override
    Future<? extends HttpResponse<?>> send(WebClient client) {
      HttpRequest<Buffer> builder = webClient.get("/redirect")
        .putHeader("foo", "bar");
      if (set != null) {
        builder = builder.followRedirects(set);
      }
      return builder.send();
    }

    @Override
    void assertResponse(HttpResponse<?> response) {
      if (expect) {
        assertEquals(200, response.statusCode());
        assertEquals("/ok", response.body().toString());
        assertEquals(1, response.followedRedirects().size());
        assertEquals(location, response.followedRedirects().get(0));
      } else {
        assertEquals(301, response.statusCode());
        assertEquals(location, response.getHeader("location"));
      }
    }
  }

  @Nested
  class DefaultFollowRedirectsTest extends FollowRedirectsTestBase {
    public DefaultFollowRedirectsTest() {
      super(null, true);
    }
  }

  @Nested
  class FollowRedirectsTest extends FollowRedirectsTestBase {
    public FollowRedirectsTest() {
      super(true, true);
    }
  }

  @Nested
  class DoNotFollowRedirectsTest extends FollowRedirectsTestBase {
    public DoNotFollowRedirectsTest() {
      super(false, false);
    }
  }

  private abstract static class WriteStreamBase implements WriteStream<Buffer> {

    protected Handler<Throwable> exceptionHandler;
    protected Handler<Void> drainHandler;

    @Override
    public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
      exceptionHandler = handler;
      return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
      return Future.succeededFuture();
    }

    @Override
    public Future<Void> end() {
      return Future.succeededFuture();
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
      return this;
    }

    @Override
    public boolean writeQueueFull() {
      return false;
    }

    @Override
    public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
      drainHandler = handler;
      return this;
    }
  }
}
