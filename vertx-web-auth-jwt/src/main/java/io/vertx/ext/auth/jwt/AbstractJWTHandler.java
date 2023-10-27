package io.vertx.ext.auth.jwt;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.audit.Marker;
import io.vertx.ext.auth.audit.SecurityAudit;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.common.AuthenticationContext;
import io.vertx.ext.auth.common.AuthenticationContextInternal;
import io.vertx.ext.auth.common.handler.impl.HTTPAuthorizationHandler;
import io.vertx.ext.web.handler.HttpException;

public abstract class AbstractJWTHandler<C extends AuthenticationContext> extends HTTPAuthorizationHandler<C, JWTAuth> implements JWTAuthHandler<C>, io.vertx.ext.auth.common.ScopedAuthentication<C, JWTAuthHandler<C>> {

  protected final List<String> scopes;
  protected String delimiter;

  public AbstractJWTHandler(JWTAuth authProvider, Type bearer, String realm) {
    super(authProvider, bearer, realm);
    scopes = Collections.emptyList();
    this.delimiter = " ";
  }

  public AbstractJWTHandler(JWTAuth authProvider, List<String> scopes,
    String delimiter, String realm) {
    super(authProvider, Type.BEARER, realm);
    Objects.requireNonNull(scopes, "scopes cannot be null");
    this.scopes = scopes;
    Objects.requireNonNull(delimiter, "delimiter cannot be null");
    this.delimiter = delimiter;
  }

  @Override
  public Future<User> authenticate(C context) {

    return parseAuthorization(context)
      .compose(token -> {
        int segments = 0;
        for (int i = 0; i < token.length(); i++) {
          char c = token.charAt(i);
          if (c == '.') {
            if (++segments == 3) {
              return Future.failedFuture(new HttpException(400, "Too many segments in token"));
            }
            continue;
          }
          if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
            continue;
          }
          // invalid character
          return Future.failedFuture(new HttpException(400, "Invalid character in token: " + (int) c));
        }

        final TokenCredentials credentials = new TokenCredentials(token);
        final SecurityAudit audit = ((AuthenticationContextInternal) context).securityAudit();
        audit.credentials(credentials);

        return
          authProvider
            .authenticate(new TokenCredentials(token))
            .andThen(op -> audit.audit(Marker.AUTHENTICATION, op.succeeded()))
            .recover(err -> Future.failedFuture(new HttpException(401, err)));
      });
  }

  /**
   * The default behavior for post-authentication
   */
  @Override
  public void postAuthentication(C ctx, User authenticated) {
    final User user = ctx.user().get();
    if (user == null) {
      // bad state
      fail(ctx, 403, "no user in the context");
      return;
    }
    // the user is authenticated, however the user may not have all the required scopes
    final List<String> scopes = getScopesOrSearchMetadata(this.scopes, ctx);

    if (scopes.size() > 0) {
      final JsonObject jwt = user.get("accessToken");
      if (jwt == null) {
        fail(ctx, 403, "Invalid JWT: null");
        return;
      }

      if (jwt.getValue("scope") == null) {
        fail(ctx, 403, "Invalid JWT: scope claim is required");
        return;
      }

      List<?> target;
      if (jwt.getValue("scope") instanceof String) {
        target =
          Stream.of(jwt.getString("scope")
              .split(delimiter))
            .collect(Collectors.toList());
      } else {
        target = jwt.getJsonArray("scope").getList();
      }

      if (target != null) {
        for (String scope : scopes) {
          if (!target.contains(scope)) {
            fail(ctx, 403, "JWT scopes != handler scopes");
            return;
          }
        }
      }
    }
    ctx.onContinue();
  }

  abstract protected void fail(C ctx, int code, String msg);
}
