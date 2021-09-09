package io.vertx.ext.web.client.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class OAuth2AwareInterceptor implements Handler<HttpContext<?>> {

  private final WebClientOauth2Aware parentClient;

  public OAuth2AwareInterceptor(WebClientOauth2Aware webClientOauth2Aware) {
    this.parentClient = webClientOauth2Aware;
  }

  @Override
  public void handle(HttpContext<?> context) {
    switch (context.phase()) {
      case CREATE_REQUEST:
        createRequest(context)
          .onFailure(context::fail)
          .onSuccess(done -> context.next());
        break;
      default:
        context.next();
        break;
    }
  }

  private Future<Void> createRequest(HttpContext<?> context) {

    HttpRequestImpl<?> request = (HttpRequestImpl<?>) context.request();

    Promise<Void> promise = Promise.promise();
    if (parentClient.isWithAuthentication()) {
      if (parentClient.getUser() != null) {
        if (parentClient.getUser().expired(parentClient.getLeeway())) {
          //Token has expired we need to invalidate the session
          parentClient
            .oauth2Auth()
            .refresh(parentClient.getUser())
            .onSuccess(userResult -> {
              parentClient.setUser(userResult);
              parentClient.setWithAuthentication(false);
              context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
              promise.complete();
            })
            .onFailure(error -> {
              // Refresh token failed, we can try standard authentication
              parentClient
                .oauth2Auth()
                .authenticate(parentClient.getCredentials())
                .onSuccess(userResult -> {
                  parentClient.setUser(userResult);
                  parentClient.setWithAuthentication(false);
                  context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
                  promise.complete();
                })
                .onFailure(errorAuth -> {
                  //Refresh token did not work and failed to obtain new authentication token, we need to fail
                  parentClient.setUser(null);
                  parentClient.setWithAuthentication(false);
                  promise.fail(errorAuth);
                });
            });
        } else {
          //User is not expired, access_token is valid
          parentClient.setWithAuthentication(false);
          context.requestOptions().addHeader(AUTHORIZATION, parentClient.getUser().principal().getString("access_token"));
          promise.complete();
        }
      } else {
        parentClient
          .oauth2Auth()
          .authenticate(parentClient.getCredentials())
          .onSuccess(userResult -> {
            parentClient.setUser(userResult);
            parentClient.setWithAuthentication(false);
            context.requestOptions().addHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
            promise.complete();
          })
          .onFailure(promise::fail);
      }
    } else {
      promise.complete();
    }

    return promise.future();
  }
}
