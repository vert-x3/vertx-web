package io.vertx.ext.auth.oauth2;

import java.util.List;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.common.AuthenticationContext;
import io.vertx.ext.auth.common.AuthenticationHandler;

public interface OAuth2AuthHandler<C extends AuthenticationContext> extends AuthenticationHandler<C> {

  /**
   * Extra parameters needed to be passed while requesting a token.
   *
   * @param extraParams extra optional parameters.
   * @return self
   */
  @Fluent
  OAuth2AuthHandler<C> extraParams(JsonObject extraParams);

  /**
   * Return a <b>new instance</b> with the internal state copied from the caller but the scopes to be requested during
   * a token request are unique to the instance. When scopes are applied to the handler, the default scopes from the
   * route metadata will be ignored.
   *
   * @param scope scope.
   * @return new instance of this interface.
   */
  @Fluent
  OAuth2AuthHandler<C> withScope(String scope);

  /**
   * Return a <b>new instance</b> with the internal state copied from the caller but the scopes to be requested during
   * a token request are unique to the instance. When scopes are applied to the handler, the default scopes from the
   * route metadata will be ignored.
   *
   * @param scopes scopes.
   * @return new instance of this interface.
   */
  @Fluent
  OAuth2AuthHandler<C> withScopes(List<String> scopes);

  /**
   * Indicates the type of user interaction that is required. Not all providers support this or the full list.
   *
   * Well known values are:
   *
   * <ul>
   *   <li><b>login</b> will force the user to enter their credentials on that request, negating single-sign on.</li>
   *   <li><b>none</b> is the opposite - it will ensure that the user isn't presented with any interactive prompt
   *      whatsoever. If the request can't be completed silently via single-sign on, the Microsoft identity platform
   *      endpoint will return an interaction_required error.</li>
   *   <li><b>consent</b> will trigger the OAuth consent dialog after the user signs in, asking the user to grant
   *      permissions to the app.</li>
   *   <li><b>select_account</b> will interrupt single sign-on providing account selection experience listing all the
   *      accounts either in session or any remembered account or an option to choose to use a different account
   *      altogether.</li>
   *   <li><b></b></li>
   * </ul>
   *
   * @param prompt the prompt choice.
   * @return self
   */
  @Fluent
  OAuth2AuthHandler<C> prompt(String prompt);

  /**
   * PKCE (RFC 7636) is an extension to the Authorization Code flow to prevent several attacks and to be able to
   * securely perform the OAuth exchange from public clients.
   *
   * It was originally designed to protect mobile apps, but its ability to prevent authorization code injection
   * makes it useful for every OAuth client, even web apps that use a client secret.
   *
   * @param length A number between 43 and 128. Or -1 to disable.
   * @return self
   */
  @Fluent
  OAuth2AuthHandler<C> pkceVerifierLength(int length);

}
