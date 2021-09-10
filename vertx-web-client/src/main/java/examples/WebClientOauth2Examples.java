package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOAuth2;
import io.vertx.ext.web.client.WebClientSession;

public class WebClientOauth2Examples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    WebClientOAuth2 oauth2 = WebClientOAuth2.create(
        client,
        OAuth2Auth.create(vertx, new OAuth2Options(/* enter IdP config */)))

      // configure the initial credentials (needed to fetch if needed
      // the access_token
      .withCredentials(new TokenCredentials("some.jwt.token"));
  }

  public void discovery(Vertx vertx) {
    KeycloakAuth.discover(
        vertx,
        new OAuth2Options().setSite("https://keycloakserver.com"))
      .onSuccess(oauth -> {
        WebClientOAuth2 client = WebClientOAuth2.create(
            WebClient.create(vertx),
            oauth)
          // if your keycloak is configured for password_credentials_flow
          .withCredentials(
            new UsernamePasswordCredentials("bob", "s3cret"));
      });
  }

  public void leeway(WebClient baseClient, OAuth2Auth oAuth2Auth) {
    WebClientOAuth2 client = WebClientOAuth2.create(
        baseClient,
        oAuth2Auth)
      .leeway(5);
  }

}
