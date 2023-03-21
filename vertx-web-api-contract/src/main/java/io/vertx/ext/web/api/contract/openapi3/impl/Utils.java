package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.ResolverCache;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

  public static void create(Vertx vertx,
                     String url,
                     List<JsonObject> auth,
                     Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    List<AuthorizationValue> authorizationValues = auth.stream()
      .map(obj -> {
        AuthorizationValue authorizationValue = obj.mapTo(AuthorizationValue.class);
        authorizationValue.setUrlMatcher(u -> true);
        return authorizationValue;
      })
      .collect(Collectors.toList());
    vertx.executeBlocking((Promise<OpenAPI3RouterFactory> future) -> {
      SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser()
        .readLocation(url, authorizationValues, OpenApi3Utils.getParseOptions());
      if (swaggerParseResult.getMessages().isEmpty()) {
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, swaggerParseResult.getOpenAPI(), new ResolverCache(swaggerParseResult.getOpenAPI(), null, url)));
      } else {
        if (swaggerParseResult.getMessages().size() == 1 && swaggerParseResult.getMessages().get(0).matches("Unable to read location `?\\Q" + url + "\\E`?"))
          future.fail(RouterFactoryException.createSpecNotExistsException(url));
        else
          future.fail(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(), ", ")));
      }
    }, handler);
  }
}
