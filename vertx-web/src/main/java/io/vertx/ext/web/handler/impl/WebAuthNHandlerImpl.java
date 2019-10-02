package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.webauthn.CredentialsChallengeType;
import io.vertx.ext.auth.webauthn.WebAuthN;
import io.vertx.ext.auth.webauthn.WebAuthNInfo;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.WebAuthNHandler;

public class WebAuthNHandlerImpl implements WebAuthNHandler {

  private static final Logger LOG = LoggerFactory.getLogger(WebAuthNHandlerImpl.class);

  private final WebAuthN webAuthN;
  // the extra routes
  private Route register = null;
  private Route login = null;
  private Route response = null;


  public WebAuthNHandlerImpl(WebAuthN webAuthN) {
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

  private static boolean matchesRoute(RoutingContext ctx, Route route) {
    if (route != null) {
      return ctx.request().method() == HttpMethod.POST && ctx.normalisedPath().equals(route.getPath());
    }
    return false;
  }

  @Override
  public void handle(RoutingContext ctx) {

    if (response == null) {
      LOG.error("No callback mounted!");
      ctx.fail(500);
      return;
    }

    if (matchesRoute(ctx, response)) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("The callback route is shaded by the WebAuthNAuthHandler, ensure the callback route is added BEFORE the WebAuthNAuthHandler route!");
      }
      ctx.fail(500);
      return;
    }

    if (matchesRoute(ctx, register)) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("The register callback route is shaded by the WebAuthNAuthHandler, ensure the callback route is added BEFORE the WebAuthNAuthHandler route!");
      }
      ctx.fail(500);
      return;
    }

    if (matchesRoute(ctx, login)) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("The login callback route is shaded by the WebAuthNAuthHandler, ensure the callback route is added BEFORE the WebAuthNAuthHandler route!");
      }
      ctx.fail(500);
      return;
    }

    if (ctx.user() == null) {
      ctx.fail(401);
    } else {
      ctx.next();
    }
  }


  @Override
  public WebAuthNHandler setupCredentialsCreateCallback(Route route) {
    this.register = route
      // force a post if otherwise
      .method(HttpMethod.POST)
      .handler(ctx -> {
        try {
          // might throw runtime exception if there's no json or is bad formed
          final JsonObject webauthnRegister = ctx.getBodyAsJson();
          final Session session = ctx.session();

          if (isEmptyString(webauthnRegister, "name") || isEmptyString(webauthnRegister, "displayName") || isEmptyString(webauthnRegister, "type")) {
            ctx.fail(400);
          } else {
            // input basic validation is OK

            if (session == null) {
              LOG.warn("No session or session handler is missing.");
              ctx.fail(500);
              return;
            }

            CredentialsChallengeType challengeType;

            switch (webauthnRegister.getString("type")) {
              case "cross-platform":
                challengeType = CredentialsChallengeType.CROSS_PLATFORM;
                break;
              case "platform":
                challengeType = CredentialsChallengeType.PLATFORM;
                break;
              default:
                ctx.fail(400);
                return;
            }

            webAuthN.createCredentialsOptions(webauthnRegister, challengeType, createCredentialsOptions -> {
              if (createCredentialsOptions.failed()) {
                ctx.fail(createCredentialsOptions.cause());
                return;
              }

              final JsonObject credentialsOptions = createCredentialsOptions.result();

              // save challenge to the session
              ctx.session()
                .put("challenge", credentialsOptions.getString("challenge"))
                .put("username", webauthnRegister.getString("name"));

              ctx.json(credentialsOptions);
            });
          }
        } catch (IllegalArgumentException e) {
          ctx.fail(400);
        } catch (RuntimeException e) {
          LOG.error("Unexpected exception", e);
          ctx.fail(e);
        }
      });
      return this;
  }

  @Override
  public WebAuthNHandler setupCredentialsGetCallback(Route route) {
    this.login = route
      // force a post if otherwise
      .method(HttpMethod.POST)
      .handler(ctx -> {
        try {
          // might throw runtime exception if there's no json or is bad formed
          final JsonObject webauthnLogin = ctx.getBodyAsJson();
          final Session session = ctx.session();

          if (isEmptyString(webauthnLogin, "name")) {
            LOG.debug("Request missing username field");
            ctx.fail(400);
            return;
          }

          // input basic validation is OK

          if (session == null) {
            LOG.warn("No session or session handler is missing.");
            ctx.fail(500);
            return;
          }

          final String username = webauthnLogin.getString("name");

          // STEP 18 Generate assertion
          webAuthN.getCredentialsOptions(username, generateServerGetAssertion -> {
            if (generateServerGetAssertion.failed()) {
              LOG.error("Unexpected exception", generateServerGetAssertion.cause());
              ctx.fail(generateServerGetAssertion.cause());
              return;
            }

            final JsonObject getAssertion = generateServerGetAssertion.result();

            session
              .put("challenge", getAssertion.getString("challenge"))
              .put("username", username);

            ctx.json(getAssertion);
          });
        } catch (IllegalArgumentException e) {
          LOG.error("Unexpected exception", e);
          ctx.fail(400);
        } catch (RuntimeException e) {
          LOG.error("Unexpected exception", e);
          ctx.fail(e);
        }
      });
    return this;
  }

  @Override
  public WebAuthNHandler setupCallback(Route route) {
    this.response = route
      // force a post if otherwise
      .method(HttpMethod.POST)
      .handler(ctx -> {
        try {
          // might throw runtime exception if there's no json or is bad formed
          final JsonObject webauthnResp = ctx.getBodyAsJson();
          // input validation
          if (
            isEmptyString(webauthnResp, "id") ||
              isEmptyString(webauthnResp, "rawId") ||
              isEmptyObject(webauthnResp, "response") ||
              isEmptyString(webauthnResp, "type") ||
              !"public-key".equals(webauthnResp.getString("type"))) {

            LOG.debug("Response missing one or more of id/rawId/response/type fields, or type is not public-key");
            ctx.fail(400);
            return;
          }

          // input basic validation is OK

          final Session session = ctx.session();

          if (ctx.session() == null) {
            LOG.error("No session or session handler is missing.");
            ctx.fail(500);
            return;
          }

          webAuthN.authenticate(
            // authInfo
            new WebAuthNInfo()
              .setChallenge(session.get("challenge"))
              .setUsername(session.get("username"))
              .setWebauthn(webauthnResp), authenticate -> {

              // invalidate the challenge
              session.remove("challenge");

              if (authenticate.succeeded()) {
                final User user = authenticate.result();
                // save the user into the context
                ctx.setUser(user);
                // the user has upgraded from unauthenticated to authenticated
                // session should be upgraded as recommended by owasp
                session.regenerateId();
                ctx.response().end();
              } else {
                LOG.error("Unexpected exception", authenticate.cause());
                ctx.fail(authenticate.cause());
              }
            });
        } catch (IllegalArgumentException e) {
          LOG.error("Unexpected exception", e);
          ctx.fail(400);
        } catch (RuntimeException e) {
          LOG.error("Unexpected exception", e);
          ctx.fail(e);
        }
      });

    return this;
  }
}
