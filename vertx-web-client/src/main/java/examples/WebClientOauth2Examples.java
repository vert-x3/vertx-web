package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.web.client.OAuth2WebClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.OAuth2WebClientOptions;

public class WebClientOauth2Examples {

  public void create(Vertx vertx) {
    WebClient client = WebClient.create(vertx);
    OAuth2WebClient oauth2 = OAuth2WebClient.create(
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
        OAuth2WebClient client = OAuth2WebClient.create(
            WebClient.create(vertx),
            oauth)
          // if your keycloak is configured for password_credentials_flow
          .withCredentials(
            new UsernamePasswordCredentials("bob", "s3cret"));
      });
  }

  public void leeway(WebClient baseClient, OAuth2Auth oAuth2Auth) {
    OAuth2WebClient client = OAuth2WebClient.create(
        baseClient,
        oAuth2Auth,
        new OAuth2WebClientOptions()
          .setLeeway(5));
  }

  public void renewTokenOnForbidden(WebClient baseClient, OAuth2Auth oAuth2Auth) {
    OAuth2WebClient client = OAuth2WebClient.create(
      baseClient,
      oAuth2Auth,
      new OAuth2WebClientOptions()
        // the client will attempt a single token request, if the request
        // if the status code of the response is 401
        // there will be only 1 attempt, so the second consecutive 401
        // will be passed down to your handler/promise
        .setRenewTokenOnForbidden(true));
  }
}
