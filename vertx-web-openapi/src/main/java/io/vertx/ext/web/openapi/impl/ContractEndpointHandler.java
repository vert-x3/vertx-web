package io.vertx.ext.web.openapi.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
    JsonObject openapi = holder.getOpenAPI();

    ObjectMapper jsonMapper = new JsonMapper();
    try {
      JsonNode node = jsonMapper.readTree(openapi.toString());
      ObjectMapper yamlMapper = new YAMLMapper();
      byte[] yamlBytes = yamlMapper.writeValueAsBytes(node);
      return new ContractEndpointHandler(openapi.toBuffer(), Buffer.buffer(yamlBytes));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RouterBuilderException("Cannot generate yaml contract",
        ErrorType.UNSUPPORTED_SPEC, e);
    }
  }

}
