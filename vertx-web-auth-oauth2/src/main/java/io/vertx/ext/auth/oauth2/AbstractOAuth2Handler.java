package io.vertx.ext.auth.oauth2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.auth.audit.Marker;
import io.vertx.ext.auth.audit.SecurityAudit;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.common.AuthenticationContext;
import io.vertx.ext.auth.common.AuthenticationContextInternal;
import io.vertx.ext.auth.common.Session;
import io.vertx.ext.auth.common.handler.impl.HTTPAuthorizationHandler;
import io.vertx.ext.auth.impl.Codec;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.impl.Origin;

public abstract class AbstractOAuth2Handler<C extends AuthenticationContext> extends HTTPAuthorizationHandler<C, OAuth2Auth> implements OAuth2AuthHandler<C>, io.vertx.ext.auth.common.ScopedAuthentication<C, OAuth2AuthHandler<C>> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractOAuth2Handler.class);

  protected final VertxContextPRNG prng;
  protected final Origin callbackURL;
  protected final MessageDigest sha256;

  protected final List<String> scopes;
  protected JsonObject extraParams;
  protected String prompt;
  protected int pkce = -1;
  // explicit signal that tokens are handled as bearer only (meaning, no backend server known)
  protected boolean bearerOnly = true;
  
  public AbstractOAuth2Handler(Vertx vertx, OAuth2Auth authProvider, String callbackURL, String realm) {
    super(authProvider, Type.BEARER, realm);

    // get a reference to the prng
    this.prng = VertxContextPRNG.current(vertx);
    // get a reference to the sha-256 digest
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot get instance of SHA-256 MessageDigest", e);
    }
    // process callback
    if (callbackURL != null) {
      this.callbackURL = Origin.parse(callbackURL);
    } else {
      this.callbackURL = null;
    }
    // scopes are empty by default
    this.scopes = Collections.emptyList();
  }

  protected AbstractOAuth2Handler(AbstractOAuth2Handler<?> base, List<String> scopes) {
    super(base.authProvider, Type.BEARER, base.realm);
    this.prng = base.prng;
    this.callbackURL = base.callbackURL;
    this.prompt = base.prompt;
    this.pkce = base.pkce;
    this.bearerOnly = base.bearerOnly;

    // get a new reference to the sha-256 digest
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot get instance of SHA-256 MessageDigest", e);
    }
    // state copy
    if (base.extraParams != null) {
      extraParams = base.extraParams.copy();
    }

    // apply the new scopes
    Objects.requireNonNull(scopes, "scopes cannot be null");
    this.scopes = scopes;
  }

  @Override
  public Future<User> authenticate(C context) {
    // when the handler is working as bearer only, then the `Authorization` header is required
    return parseAuthorization(context, !bearerOnly)
      .compose(token -> {
      // Authorization header can be null when in not in bearerOnly mode
      if (token == null) {
        // redirect request to the oauth2 server as we know nothing about this request
        if (bearerOnly) {
          // it's a failure both cases but the cause is not the same
          return Future.failedFuture("callback route is not configured.");
        }
        // when this handle is mounted as a catch all, the callback route must be configured before,
        // as it would shade the callback route. When a request matches the callback path and has the
        // method GET the exceptional case should not redirect to the oauth2 server as it would become
        // an infinite redirect loop. In this case an exception must be raised.
        if (context.request().method() == HttpMethod.GET && context.normalizedPath().equals(callbackURL.resource())) {
          LOG.warn("The callback route is shaded by the OAuth2AuthHandler, ensure the callback route is added BEFORE the OAuth2AuthHandler route!");
          return Future.failedFuture(new HttpException(500, "Infinite redirect loop [oauth2 callback]"));
        } else {
          if (context.request().method() != HttpMethod.GET) {
            // we can only redirect GET requests
            LOG.error("OAuth2 redirect attempt to non GET resource");
            return Future.failedFuture(new HttpException(405, new IllegalStateException("OAuth2 redirect attempt to non GET resource")));
          }

          // the redirect is processed as a failure to abort the chain
          String redirectUri = context.request().uri();
          try {
            return Future.failedFuture(new HttpException(302, authURI(context, redirectUri)));
          } catch (IllegalStateException e) {
            return Future.failedFuture(e);
          }
        }
      } else {
        // continue
        final List<String> scopes = getScopesOrSearchMetadata(this.scopes, context);

        final Credentials credentials =
          scopes.size() > 0 ? new TokenCredentials(token).setScopes(scopes) : new TokenCredentials(token);

        final SecurityAudit audit = ((AuthenticationContextInternal) context).securityAudit();
        audit.credentials(credentials);

        return authProvider.authenticate(credentials)
          .andThen(op -> audit.audit(Marker.AUTHENTICATION, op.succeeded()))
          .recover(err -> Future.failedFuture(new HttpException(401, err)));
      }
    });
  }

  private String authURI(C context, String redirectURL) {

    String state = null;
    String codeVerifier = null;
    String loginHint = null;

    final Session session = context.session();

    if (session == null) {
      if (pkce > 0) {
        // we can only handle PKCE with a session
        throw new IllegalStateException("OAuth2 PKCE requires a session to be present");
      }
    } else {
      // there's a session we can make this request comply to the Oauth2 spec and add an opaque state

      loginHint = session.get("login_hint");
      // hint will be considered at least once
      session.remove("login_hint");

      session
        .put("redirect_uri", redirectURL);

      // create a state value to mitigate replay attacks
      state = prng.nextString(6);
      // store the state in the session
      session
        .put("state", state);

      if (pkce > 0) {
        codeVerifier = prng.nextString(pkce);
        // store the code verifier in the session
        session
          .put("pkce", codeVerifier);
      }
    }

    final OAuth2AuthorizationURL config = new OAuth2AuthorizationURL();

    if (extraParams != null) {
      for (Map.Entry<String, Object> entry : extraParams) {
        if (entry.getValue() != null) {
          config.putAdditionalParameter(entry.getKey(), entry.getValue().toString());
        }
      }
    }

    config
      .setState(state != null ? state : redirectURL)
      .setLoginHint(loginHint)
      .setPrompt(prompt);

    if (callbackURL != null) {
      config.setRedirectUri(callbackURL.href());
    }

    final List<String> scopes = getScopesOrSearchMetadata(this.scopes, context);

    if (scopes.size() > 0) {
      config.setScopes(scopes);
    }

    if (codeVerifier != null) {
      synchronized (sha256) {
        sha256.update(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        config
          .setCodeChallenge(Codec.base64UrlEncode(sha256.digest()))
          .setCodeChallengeMethod("S256");
      }
    }

    return authProvider.authorizeURL(new OAuth2AuthorizationURL(config));
  }

  @Override
  public OAuth2AuthHandler<C> extraParams(JsonObject extraParams) {
    this.extraParams = extraParams;
    return this;
  }

  @Override
  public OAuth2AuthHandler<C> prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  @Override
  public OAuth2AuthHandler<C> pkceVerifierLength(int length) {
    if (length >= 0) {
      // requires verification
      if (length < 43 || length > 128) {
        throw new IllegalArgumentException("Length must be between 34 and 128");
      }
    }
    this.pkce = length;
    return this;
  }



  private static final Set<String> OPENID_SCOPES = new HashSet<>();

  static {
    OPENID_SCOPES.add("openid");
    OPENID_SCOPES.add("profile");
    OPENID_SCOPES.add("email");
    OPENID_SCOPES.add("phone");
    OPENID_SCOPES.add("offline");
  }

  /**
   * The default behavior for post-authentication
   */
  @Override
  public void postAuthentication(C ctx, User authenticatedUser) {
    // the user is authenticated, however the user may not have all the required scopes
    final List<String> scopes = getScopesOrSearchMetadata(this.scopes, ctx);

    if (scopes.size() > 0) {
      final User user = ctx.user().get();
      if (user == null) {
        // bad state
        fail(ctx, 403, "no user in the context");
        return;
      }

      if (user.principal().containsKey("scope")) {
        final String userScopes = user.principal().getString("scope");
        if (userScopes != null) {
          // user principal contains scope, a basic assertion is required to ensure that
          // the scopes present match the required ones

          // check if openid is active
          final boolean openId = userScopes.contains("openid");

          for (String scope : scopes) {
            // do not assert openid scopes if openid is active
            if (openId && OPENID_SCOPES.contains(scope)) {
              continue;
            }

            int idx = userScopes.indexOf(scope);
            if (idx != -1) {
              // match, but is it valid?
              if (
                (idx != 0 && userScopes.charAt(idx -1) != ' ') ||
                  (idx + scope.length() != userScopes.length() && userScopes.charAt(idx + scope.length()) != ' ')) {
                // invalid scope assignment
                fail(ctx, 403, "principal scope != handler scopes");
                return;
              }
            } else {
              // invalid scope assignment
              fail(ctx, 403, "principal scope != handler scopes");
              return;
            }
          }
        }
      }
    }
    ctx.onContinue();
  }

  @Override
  public boolean performsRedirect() {
    // depending on the time this method is invoked
    // we can deduct with more accuracy if a redirect is possible or not
    if (!bearerOnly) {
      // we know that a redirect is definitely possible
      // as the callback handler has been created
      return true;
    } else {
      // the callback hasn't been mounted so we need to assume
      // that if no callbackURL is provided, then there isn't
      // a redirect happening in this application
      return callbackURL != null;
    }
  }

  abstract protected void fail(C ctx, int code, String msg);
}
