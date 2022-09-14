package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.ParsableMIMEValue;
import io.vertx.ext.web.openapi.ErrorType;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.RouterBuilderException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContractEndpointHandler implements Handler<RoutingContext> {

  private static final List<MIMEHeader> JSON_DATA_TYPES = Stream.of(
    new ParsableMIMEValue("application/json").forceParse()
  ).collect(Collectors.toList());

  private final Buffer openapiJson;
  private final Buffer openapiYaml;

  private ContractEndpointHandler(Buffer openapiJson, Buffer openapiYaml) {
    this.openapiJson = openapiJson;
    this.openapiYaml = openapiYaml;
  }

  @Override
  public void handle(RoutingContext context) {
    if (responseAsJson(context)) {
      context.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(openapiJson);
      return;
    }
    context.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/yaml")
      .end(openapiYaml);
  }

  public boolean responseAsJson(RoutingContext context) {
    MIMEHeader acceptJsonHeader = context.parsedHeaders().findBestUserAcceptedIn(context.parsedHeaders().accept(),
      JSON_DATA_TYPES);
    if (acceptJsonHeader != null) {
      return true;
    }
    String format = context.queryParams().get("format");
    return format != null && format.equalsIgnoreCase("json");
  }

  public static ContractEndpointHandler create(OpenAPIHolder holder) {
    try (StringWriter writer = new StringWriter()) {
      JsonObject openapi = holder.getOpenAPI();
      Yaml yaml = new Yaml(new SafeConstructor());
      yaml.dump(openapi.getMap(), writer);
      return new ContractEndpointHandler(openapi.toBuffer(), Buffer.buffer(writer.toString()));
    } catch (IOException | RuntimeException e) {
      throw new RouterBuilderException("Cannot generate yaml contract", ErrorType.UNSUPPORTED_SPEC, e);
    }
  }

}
