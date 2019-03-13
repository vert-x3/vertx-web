package io.vertx.ext.web.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.webauthn.WebAuthN;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.WebAuthNHandler;

import java.util.Base64;

public class WebAuthNHandlerImpl implements WebAuthNHandler {

  private final Base64.Decoder base64url = Base64.getUrlDecoder();
  private final String origin;

  private WebAuthN webAuthN;

  public WebAuthNHandlerImpl(WebAuthN webAuthN, String origin) {
    if (origin == null) {
      throw new NullPointerException("origin cannot be null");
    }
    this.origin = origin;
    this.webAuthN = webAuthN;
  }

  private static boolean isEmptyString(JsonObject json, String key) {
    try {
      if (json == null) {
        return true;
      }
      if (!json.containsKey(key)) {
        return true;
      }
      String s = json.getString(key);
      return s == null || "".equals(s);
    } catch (RuntimeException e) {
      return true;
    }
  }

  private static boolean isEmptyObject(JsonObject json, String key) {
    try {
      if (json == null) {
        return true;
      }
      if (!json.containsKey(key)) {
        return true;
      }
      JsonObject s = json.getJsonObject(key);
      return s == null;
    } catch (RuntimeException e) {
      return true;
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    try {
      // might throw runtime exception if there's no json or is bad formed
      final JsonObject webauthnResp = ctx.getBodyAsJson();

      System.out.println("/response <== " + webauthnResp.encodePrettily());

      // input validation
      if (
        isEmptyString(webauthnResp, "id") ||
          isEmptyString(webauthnResp, "rawId") ||
          isEmptyObject(webauthnResp, "response") ||
          isEmptyString(webauthnResp, "type") ||
          !"public-key".equals(webauthnResp.getString("type"))) {

        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject()
            .put("status", "failed")
            .put("message", "Response missing one or more of id/rawId/response/type fields, or type is not public-key!")
            .encode());

      } else {
        // input basic validation is OK

        final Session session = ctx.session();

        // TODO: session cannot be null

        final JsonObject clientData = new JsonObject(Buffer.buffer(base64url.decode(webauthnResp.getJsonObject("response").getString("clientDataJSON"))));

        System.out.println("clientData <== " + clientData.encodePrettily());
        System.out.println("session <== " + ctx.session().data());

        /* Check challenge... */
        if (!clientData.getValue("challenge").equals(session.get("challenge"))) {

          System.out.println(clientData.getValue("challenge"));
          System.out.println((String) session.get("challenge"));

          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
              .put("status", "failed")
              .put("message", "Challenges don\'t match!")
              .encode());
        }

        /* ...and origin */
        if (!origin.equals(clientData.getValue("origin"))) {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
              .put("status", "failed")
              .put("message", "Origins don\'t match!")
              .encode());
        }

        webAuthN.authenticate(
          // authInfo
          new JsonObject()
            .put("username", session.<String>get("username"))
            .put("webauthn", webauthnResp), authenticate -> {

            if (authenticate.succeeded()) {
              final User user = authenticate.result();
              // save the user into the context
              ctx.setUser(user);
              // the user has upgraded from unauthenticated to authenticated
              // session should be upgraded as recommended by owasp
              session.regenerateId();

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                  .put("status", "ok")
                  .encode());
            } else {
              authenticate.cause().printStackTrace();
              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                  .put("status", "failed")
                  .put("message", authenticate.cause().getMessage())
                  .encode());
            }
          });
      }
    } catch (RuntimeException e) {
      ctx.fail(e);
    }
  }

  @Override
  public Handler<RoutingContext> loginHandler() {
    return ctx -> {
      try {
        // might throw runtime exception if there's no json or is bad formed
        final JsonObject webauthnLogin = ctx.getBodyAsJson();
        final Session session = ctx.session();

        System.out.println("/login <== " + webauthnLogin.encodePrettily());


        if (isEmptyString(webauthnLogin, "username")) {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
              .put("status", "failed")
              .put("message", "Request missing username field!")
              .encode());

        } else {
          // input basic validation is OK

          // TODO: session cannot be null

          final String username = webauthnLogin.getString("username");

          webAuthN.generateServerGetAssertion(username, generateServerGetAssertion -> {
            if (generateServerGetAssertion.succeeded()) {

              JsonObject assertion = generateServerGetAssertion.result();
              assertion.put("status", "ok");

              session.put("challenge", assertion.getString("challenge"));
              session.put("username", username);

              System.out.println("/login ==> " + assertion.encodePrettily());

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(assertion.encode());

            } else {
              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                  .put("status", "failed")
                  .put("message", generateServerGetAssertion.cause().getMessage())
                  .encode());
            }
          });
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
        ctx.fail(e);
      }
    };
  }

  @Override
  public Handler<RoutingContext> registerHandler() {
    return ctx -> {
      try {
        // might throw runtime exception if there's no json or is bad formed
        final JsonObject webauthnRegister = ctx.getBodyAsJson();

        System.out.println("/register");
        System.out.println(webauthnRegister.encodePrettily());

        if (isEmptyString(webauthnRegister, "username") || isEmptyString(webauthnRegister, "name")) {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
              .put("status", "failed")
              .put("message", "Request missing name or username field!")
              .encode());
        } else {
          // input basic validation is OK

          // TODO: session cannot be null

          final String username = webauthnRegister.getString("username");
          final String displayName = webauthnRegister.getString("name");

          webAuthN.generateServerMakeCredRequest(username, displayName, generateServerMakeCredRequest -> {
            if (generateServerMakeCredRequest.succeeded()) {
              final JsonObject challengeMakeCred = generateServerMakeCredRequest.result();

              System.out.println("generateServerMakeCredRequest");
              System.out.println(challengeMakeCred.encodePrettily());

              challengeMakeCred.put("status", "ok");
              // save challenge to the session
              ctx.session()
                .put("challenge", challengeMakeCred.getString("challenge"))
                .put("username", username);

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(challengeMakeCred.encode());
            } else {
              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                  .put("status", "failed")
                  .put("message", generateServerMakeCredRequest.cause().getMessage())
                  .encode());
            }
          });
        }
      } catch (RuntimeException e) {
        ctx.fail(e);
      }
    };
  }
}
