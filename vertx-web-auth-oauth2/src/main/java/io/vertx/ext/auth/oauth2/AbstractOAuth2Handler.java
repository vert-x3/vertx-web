package io.vertx.ext.auth.oauth2;

import io.vertx.ext.auth.common.AuthenticationContext;
import io.vertx.ext.auth.common.handler.impl.HTTPAuthorizationHandler;

public abstract class AbstractOAuth2Handler<C extends AuthenticationContext> extends HTTPAuthorizationHandler<C, OAuth2Auth> implements OAuth2AuthHandler<C>, io.vertx.ext.auth.common.ScopedAuthentication<C, OAuth2AuthHandler<C>> {

  public AbstractOAuth2Handler(OAuth2Auth authProvider, Type type,
    String realm) {
    super(authProvider, type, realm);
  }

}
