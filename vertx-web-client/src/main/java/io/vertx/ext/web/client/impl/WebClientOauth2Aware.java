package io.vertx.ext.web.client.impl;

import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOAuth2;

public class WebClientOauth2Aware extends WebClientBase implements WebClientOAuth2 {

  private final OAuth2Auth oauth2Auth;
  private Credentials credentials;

  private User user;

  private int leeway = 0;

  public WebClientOauth2Aware(WebClient client, OAuth2Auth oauth2Auth) {
    super((WebClientBase) client);

    if (oauth2Auth == null) {
      throw new IllegalArgumentException("OAuth2Auth cannot be null");
    }
    this.oauth2Auth = oauth2Auth;
    addInterceptor(new OAuth2AwareInterceptor(this));
  }

  @Override
  public WebClientOAuth2 leeway(int seconds) {
    this.leeway = seconds;
    return this;
  }

  @Override
  public WebClientOAuth2 withCredentials(Credentials credentials) {
     if (credentials == null) {
      throw new NullPointerException("Token Configuration passed to WebClientOauth2Aware can not be null");
    }

    if (this.credentials != null && !this.credentials.equals(credentials)) {
      //We need to invalidate the current data as new configuration is passed
      user = null;
    }

    this.credentials = credentials;
    return this;
  }

  Credentials getCredentials() {
    return credentials;
  }

  User getUser() {
    return user;
  }

  void setUser(User user) {
    this.user = user;
  }

  OAuth2Auth oauth2Auth() {
    return oauth2Auth;
  }

  public int getLeeway() {
    return leeway;
  }
}
