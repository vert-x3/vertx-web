package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.body.FormBodyProcessorImpl;
import io.vertx.ext.web.validation.impl.body.FormValueParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class MultipartFormBodyProcessorGenerator implements BodyProcessorGenerator {

  @Override
  public boolean canGenerate(String mediaTypeName, JsonObject mediaTypeObject) {
    return mediaTypeName.equals("multipart/form-data");
  }

  @Override
  public BodyProcessor generate(String mediaTypeName, JsonObject mediaTypeObject, JsonPointer mediaTypePointer, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(
      mediaTypeObject.getJsonObject("schema", new JsonObject()),
      mediaTypePointer.copy().append("schema")
    );

    Map<String, ValueParser<List<String>>> propertiesValueParsers =
      ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema());
    Map<Pattern, ValueParser<List<String>>> patternPropertiesValueParsers =
      ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema());
    ValueParser<List<String>> additionalPropertiesValueParser =
      ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema());

    for (Entry<String, Object> pe : schemas.getFakeSchema().getJsonObject("properties", new JsonObject())) {
      JsonObject propSchema = (JsonObject) pe.getValue();
      String encoding = (String) JsonPointer.create().append("encoding").append(pe.getKey()).append("contentType").queryJson(mediaTypeObject);

      if (encoding == null) {
        if (OpenApi3Utils.isSchemaObjectOrCombinators(propSchema) ||
          (OpenApi3Utils.isSchemaArray(propSchema) &&
            OpenApi3Utils.isSchemaObjectOrAllOfType((propSchema.getJsonObject("items", new JsonObject()))))) {
          propertiesValueParsers.put(pe.getKey(), new FormValueParser(false, ValueParser.JSON_PARSER));
        } else if ("string".equals(propSchema.getString("type")) &&
          ("binary".equals(propSchema.getString("format")) || "base64".equals(propSchema.getString("format")))) {
          context.addPredicate(
            RequestPredicate.multipartFileUploadExists(pe.getKey(), Pattern.quote("application/octet-stream"))
          );
          propertiesValueParsers.remove(pe.getKey());
          searchPropAndRemoveInSchema(schemas.getNormalizedSchema(), pe.getKey());
        }
      } else {
        context.addPredicate(
          RequestPredicate.multipartFileUploadExists(pe.getKey(), OpenApi3Utils.resolveContentTypeRegex(encoding))
        );
        propertiesValueParsers.remove(pe.getKey());
        searchPropAndRemoveInSchema(schemas.getNormalizedSchema(), pe.getKey());
      }
    }

    return new FormBodyProcessorImpl(
      propertiesValueParsers,
      patternPropertiesValueParsers,
      additionalPropertiesValueParser,
      mediaTypeName,
      schemas.getValidator()
    );
  }

  private void searchPropAndRemoveInSchema(JsonObject object, String propName) {
    if (object.containsKey("allOf") || object.containsKey("anyOf") || object.containsKey("oneOf")) {
      object.getJsonArray("allOf", object.getJsonArray("anyOf", object.getJsonArray("oneOf")))
        .forEach(j -> searchPropAndRemoveInSchema((JsonObject) j, propName));
    } else {
      if (object.containsKey("properties")) {
        object.getJsonObject("properties").remove(propName);
      }
      if (object.containsKey("required")) {
        object.getJsonArray("required").remove(propName);
      }
    }
  }
}
