package io.vertx.ext.web.handler;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.otp.Authenticator;
import io.vertx.ext.auth.otp.hotp.HotpAuth;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class OtpHandlerTest  extends WebTestBase {

  static class DummyDatabase {

    private final Map<String, Authenticator> DB = new ConcurrentHashMap<>();

    public Future<Authenticator> fetch(String id) {
      if (DB.containsKey(id)) {
        return Future.succeededFuture(DB.get(id));
      } else {
        return Future.succeededFuture();
      }
    }

    public Future<Void> upsert(Authenticator authenticator) {
      DB.put(authenticator.getIdentifier(), authenticator);
      return Future.succeededFuture();
    }

    public DummyDatabase fixture(Authenticator authenticator) {
      DB.put(authenticator.getIdentifier(), authenticator);
      return this;
    }

    public void dump() {
      DB.values().forEach(authr -> System.out.println(authr.toJson().encodePrettily()));
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    router.post()
      .handler(BodyHandler.create());

    router.route()
      .handler(SessionHandler
        .create(LocalSessionStore.create(vertx))
        .setCookieSameSite(CookieSameSite.STRICT));

    router.route()
      .handler(BasicAuthHandler.create(PropertyFileAuthentication.create(vertx, "login/loginusers.properties")));
  }

  @Test
  public void testWithoutReroute() throws Exception {

    // begin OTP handler related callbacks
    final DummyDatabase db = new DummyDatabase();

    final OtpAuthHandler otp = OtpAuthHandler
      .create(HotpAuth.create()
        .authenticatorFetcher(db::fetch)
        .authenticatorUpdater(db::upsert))
      // the issuer for the application
      .issuer("Vert.x Demo")
      // handle registration of authenticators
      .setupRegisterCallback(router.post("/otp/register"))
      // handle verification of authenticators
      .setupCallback(router.post("/otp/verify"));

    // secure the rest of the routes
    router.route()
      .handler(otp);

    router.route().handler(ctx -> {
      ctx.end("OTP OK");
    });

    // Trigger 401 by Basic Auth
    testRequest(HttpMethod.GET, "/protected", 401, "Unauthorized");

    // Trigger 401 by OTP Auth
    testRequest(HttpMethod.GET, "/protected", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw=="), 401, "Unauthorized", "Unauthorized");
  }

  @Test
  public void testWithReroute() throws Exception {

    // begin OTP handler related callbacks
    final DummyDatabase db = new DummyDatabase();

    final OtpAuthHandler otp = OtpAuthHandler
      .create(HotpAuth.create()
        .authenticatorFetcher(db::fetch)
        .authenticatorUpdater(db::upsert))
      // the issuer for the application
      .issuer("Vert.x Demo")
      // redirect
      .verifyUrl("/otp/verify.html")
      // handle registration of authenticators
      .setupRegisterCallback(router.post("/otp/register"))
      // handle verification of authenticators
      .setupCallback(router.post("/otp/verify"));

    // secure the rest of the routes
    router.route()
      .handler(otp);

    router.route().handler(ctx -> {
      ctx.end("OTP OK");
    });

    // Trigger 401 by Basic Auth
    testRequest(HttpMethod.GET, "/protected", 401, "Unauthorized");

    // Trigger 302 by OTP Auth
    testRequest(HttpMethod.GET, "/protected", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw=="), 302, "Found", "Redirecting to /otp/verify.html.");
  }


  @Test
  public void testRegisterAuthenticator() throws Exception {

    // begin OTP handler related callbacks
    final DummyDatabase db = new DummyDatabase();

    final OtpAuthHandler otp = OtpAuthHandler
      .create(HotpAuth.create()
        .authenticatorFetcher(db::fetch)
        .authenticatorUpdater(db::upsert))
      // the issuer for the application
      .issuer("Vert.x Demo")
      // redirect
      .verifyUrl("/otp/verify.html")
      // handle registration of authenticators
      .setupRegisterCallback(router.post("/otp/register"))
      // handle verification of authenticators
      .setupCallback(router.post("/otp/verify"));

    // secure the rest of the routes
    router.route()
      .handler(otp);

    router.route().handler(ctx -> {
      ctx.end("OTP OK");
    });

    // Trigger 200 by OTP Auth
    testRequest(
      HttpMethod.POST,
      "/otp/register",
      req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw=="),
      res -> {
        res.body()
          .onFailure(this::fail)
          .onSuccess(body -> {
            try {
              JsonObject json = new JsonObject(body);
              assertEquals("Vert.x Demo", json.getString("issuer"));
              assertNotNull(json.getString("url"));
              assertTrue(json.getString("url").startsWith("otpauth://hotp/Vert.x+Demo:tim?secret="));
              assertTrue(json.getString("url").endsWith("&counter=0"));
              testComplete();
            } catch (Exception e) {
              fail(e);
            }
          });
      },
      200,
      "OK",
      null);

    await();
  }

  @Test
  public void testVerifyAuthenticatorBadCode() throws Exception {

    // begin OTP handler related callbacks
    final DummyDatabase db = new DummyDatabase();

    db.fixture(new Authenticator()
      .setAlgorithm("SHA1")
      .setCounter(0)
      .setIdentifier("tim")
      .setKey("FNQTLXVB74MKCGYYHXBKEKCGAHPXK7ED"));

    final OtpAuthHandler otp = OtpAuthHandler
      .create(HotpAuth.create()
        .authenticatorFetcher(db::fetch)
        .authenticatorUpdater(db::upsert))
      // the issuer for the application
      .issuer("Vert.x Demo")
      // redirect
      .verifyUrl("/otp/verify.html")
      // handle registration of authenticators
      .setupRegisterCallback(router.post("/otp/register"))
      // handle verification of authenticators
      .setupCallback(router.post("/otp/verify"));

    // secure the rest of the routes
    router.route()
      .handler(otp);

    router.route().handler(ctx -> {
      ctx.end("OTP OK");
    });

    // Trigger 401 by OTP Auth
    testRequest(
      HttpMethod.POST,
      "/otp/verify",
      req -> {
        req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw==");

        String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
        Buffer buffer = Buffer.buffer();
        String str =
          "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"code\"\r\n\r\n000000\r\n" +
            "--" + boundary + "--\r\n";
        buffer.appendString(str);
        req.putHeader("content-length", String.valueOf(buffer.length()));
        req.putHeader("content-type", "multipart/form-data; boundary=" + boundary);
        req.write(buffer);
      },
      401,
      "Unauthorized",
      null);
  }

  @Test
  public void testVerifyAuthenticatorGoodCode() throws Exception {

    // begin OTP handler related callbacks
    final DummyDatabase db = new DummyDatabase();

    db.fixture(new Authenticator()
      .setAlgorithm("SHA1")
      .setCounter(0)
      .setIdentifier("tim")
      .setKey("FNQTLXVB74MKCGYYHXBKEKCGAHPXK7ED"));

    final OtpAuthHandler otp = OtpAuthHandler
      .create(HotpAuth.create()
        .authenticatorFetcher(db::fetch)
        .authenticatorUpdater(db::upsert))
      // the issuer for the application
      .issuer("Vert.x Demo")
      // redirect
      .verifyUrl("/otp/verify.html")
      // handle registration of authenticators
      .setupRegisterCallback(router.post("/otp/register"))
      // handle verification of authenticators
      .setupCallback(router.post("/otp/verify"));

    // secure the rest of the routes
    router.route()
      .handler(otp);

    router.route().handler(ctx -> {
      ctx.end("OTP OK");
    });

    AtomicReference<String> rSetCookie = new AtomicReference<>();

    // Trigger 302 by OTP Auth
    testRequest(
      HttpMethod.POST,
      "/otp/verify",
      req -> {
        req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw==");

        String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
        Buffer buffer = Buffer.buffer();
        String str =
          "--" + boundary + "\r\n" +

            // oathtool --hotp -c 1 --base32 FNQTLXVB74MKCGYYHXBKEKCGAHPXK7ED

            "Content-Disposition: form-data; name=\"code\"\r\n\r\n793127\r\n" +
            "--" + boundary + "--\r\n";
        buffer.appendString(str);
        req.putHeader("content-length", String.valueOf(buffer.length()));
        req.putHeader("content-type", "multipart/form-data; boundary=" + boundary);
        req.write(buffer);
      },
      res -> {
        String setCookie = res.headers().get("set-cookie");
        rSetCookie.set(setCookie);
      },
      302,
      "Found",
      "Redirecting to /.");

    // try to go to the end of the chain
    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("cookie", rSetCookie.get()),
      200,
      "OK",
      "OTP OK");

  }
}
