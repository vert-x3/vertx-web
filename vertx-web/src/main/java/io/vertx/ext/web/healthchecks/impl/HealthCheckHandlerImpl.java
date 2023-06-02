package io.vertx.ext.web.healthchecks.impl;

import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.impl.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.vertx.ext.healthchecks.CheckResult.isUp;

public class HealthCheckHandlerImpl implements HealthCheckHandler {

  private final Logger log = LoggerFactory.getLogger(HealthCheckHandler.class);

  private final HealthChecks healthChecks;
  private final AuthenticationProvider authProvider;
  private volatile Function<CheckResult, Future<CheckResult>> resultMapper;

  public HealthCheckHandlerImpl(Vertx vertx, AuthenticationProvider provider) {
    this.healthChecks = HealthChecks.create(vertx);
    this.authProvider = provider;
  }

  public HealthCheckHandlerImpl(HealthChecks hc, AuthenticationProvider provider) {
    this.healthChecks = Objects.requireNonNull(hc);
    this.authProvider = provider;
  }

  @Override
  public HealthCheckHandler register(String name, Handler<Promise<Status>> procedure) {
    healthChecks.register(name, procedure);
    return this;
  }

  @Override
  public HealthCheckHandler register(String name, long timeout, Handler<Promise<Status>> procedure) {
    healthChecks.register(name, timeout, procedure);
    return this;
  }

  @Override
  public HealthCheckHandler resultMapper(Function<CheckResult, Future<CheckResult>> resultMapper) {
    this.resultMapper = resultMapper;
    return this;
  }

  @Override
  public void handle(RoutingContext rc) {
    // ensure that we get the right offset to the router, either root or sub-router
    String path = Utils.pathOffset(rc.normalizedPath(), rc);

    String id;

    // remove the leading slash to extract the id
    if (path.length() > 0) {
      id = path.substring(1);
    } else {
      id = path;
    }

    if (authProvider != null) {
      // Copy all HTTP header in a json array and params
      JsonObject authData = new JsonObject();
      rc.request().headers().forEach(entry -> authData.put(entry.getKey(), entry.getValue()));
      rc.request().params().forEach(entry -> authData.put(entry.getKey(), entry.getValue()));
      if (rc.request().method() == HttpMethod.POST
        && rc.request().getHeader(HttpHeaders.CONTENT_TYPE) != null
        && rc.request().getHeader(HttpHeaders.CONTENT_TYPE).contains("application/json")) {
        try {
          JsonObject body = rc.body().asJsonObject();
          if (body != null) {
            authData.mergeIn(body);
          }
        } catch (Exception err) {
          log.error("Invalid authentication json body", err);
        }
      }
      authProvider.authenticate(new JsonCredentials(authData))
        .onFailure(err -> rc.response().setStatusCode(403).end())
        .onSuccess(user -> {
          healthChecks.checkStatus(id).onComplete(healthReportHandler(rc));
        });
    } else {
      healthChecks.checkStatus(id).onComplete(healthReportHandler(rc));
    }
  }

  private Handler<AsyncResult<CheckResult>> healthReportHandler(RoutingContext rc) {
    Handler<AsyncResult<CheckResult>> handler = json -> {
      HttpServerResponse response = rc.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
      if (json.failed()) {
        if (json.cause().getMessage().toLowerCase().contains("not found")) {
          response.setStatusCode(404);
        } else {
          response.setStatusCode(400);
        }
        response.end("{\"message\": \"" + json.cause().getMessage() + "\"}");
      } else {
        buildResponse(json.result(), response);
      }
    };
    if (this.resultMapper != null) {
      Promise<CheckResult> promise = Promise.promise();
      promise.future().flatMap(resultMapper).onComplete(handler);
      return promise;
    }
    return handler;
  }

  private void buildResponse(CheckResult json, HttpServerResponse response) {
    int status = isUp(json) ? 200 : 503;

    if (status == 503 && hasProcedureError(json)) {
      status = 500;
    }

    List<CheckResult> checks = json.getChecks();
    if (status == 200 && checks != null && checks.isEmpty()) {
      // Special case, no procedure installed.
      response.setStatusCode(204).end();
      return;
    }

    response
      .setStatusCode(status)
      .end(json.toJson().encode());
  }

  @Override
  public synchronized HealthCheckHandler unregister(String name) {
    healthChecks.unregister(name);
    return this;
  }

  private boolean hasProcedureError(CheckResult json) {
    JsonObject data = json.getData();
    if (data != null && data.getBoolean("procedure-execution-failure", false)) {
      return true;
    }

    List<CheckResult> checks = json.getChecks();
    if (checks != null) {
      for (CheckResult check : checks) {
        if (hasProcedureError(check)) {
          return true;
        }
      }
    }

    return false;
  }
}
