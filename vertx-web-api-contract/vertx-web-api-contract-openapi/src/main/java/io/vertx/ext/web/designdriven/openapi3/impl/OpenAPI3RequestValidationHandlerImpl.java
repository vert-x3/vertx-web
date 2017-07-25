package io.vertx.ext.web.designdriven.openapi3.impl;

import com.reprezen.kaizen.oasparser.model3.*;
import com.reprezen.kaizen.oasparser.ovl3.SchemaImpl;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.designdriven.impl.HTTPOperationRequestValidationHandlerImpl;
import io.vertx.ext.web.designdriven.openapi3.OpenAPI3RequestValidationHandler;
import io.vertx.ext.web.validation.*;
import io.vertx.ext.web.validation.impl.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RequestValidationHandlerImpl extends HTTPOperationRequestValidationHandlerImpl<Operation> implements OpenAPI3RequestValidationHandler {

  /* I need this class to workaround the multipart validation of content types different from json and text */
  private class MultipartCustomValidator implements CustomValidator {

    Pattern contentTypePattern;
    String parameterName;

    public MultipartCustomValidator(Pattern contentTypeRegex, String parameterName) {
      this.contentTypePattern = contentTypeRegex;
      this.parameterName = parameterName;
    }

    private boolean existFileUpload(Set<FileUpload> files, String name, Pattern contentType) {
      for (FileUpload f : files) {
        if (f.name().equals(name) && contentType.matcher(f.contentType()).matches()) return true;
      }
      return false;
    }

    @Override
    public void validate(RoutingContext routingContext) throws ValidationException {
      if (!existFileUpload(routingContext.fileUploads(), parameterName, contentTypePattern)) {
        if (!routingContext.request().formAttributes().contains(parameterName))
          throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(parameterName,
            ParameterLocation.BODY_FORM);
      }
    }
  }

  private final static ParameterTypeValidator CONTENT_TYPE_VALIDATOR = new ParameterTypeValidator() {

    @Override
    public RequestParameter isValid(String value) throws ValidationException {
      return RequestParameter.create(value);
    }

    @Override
    public RequestParameter isValidCollection(List<String> value) throws ValidationException {
      if (value.size() > 1) return RequestParameter.create(value);
      else return this.isValid(value.get(0));
    }
  };

  List<Parameter> parentParams;

  /* --- Initialization functions --- */

  public OpenAPI3RequestValidationHandlerImpl(Operation pathSpec, List<Parameter> parentParams) {
    super(pathSpec);
    if (parentParams == null) this.parentParams = new ArrayList<>();
    else this.parentParams = parentParams;
    parseOperationSpec();
  }

  private List<Parameter> mergeParameters() {
    if (parentParams == null && pathSpec.getParameters() == null) {
      return new ArrayList<>();
    } else if (pathSpec.getParameters() == null) {
      return new ArrayList<>(parentParams);
    } else if (parentParams == null) {
      return new ArrayList<>(pathSpec.getParameters());
    } else {
      List<Parameter> result = new ArrayList<>(this.pathSpec.getParameters());
      List<Parameter> actualParams = new ArrayList<>(pathSpec.getParameters());
      for (int i = 0; i < parentParams.size(); i++) {
        for (int j = 0; j < actualParams.size(); j++) {
          Parameter parentParam = parentParams.get(i);
          Parameter actualParam = actualParams.get(j);
          if (!(parentParam.getIn().equalsIgnoreCase(actualParam.getIn()) && parentParam.getName().equals(actualParam
            .getName())))
            result.add(parentParam);
        }
      }
      return result;
    }
  }

  @Override
  public void parseOperationSpec() {
    // Extract from path spec parameters description
    for (Parameter opParameter : mergeParameters()) {
      this.parseParameter(opParameter);
    }
    this.parseRequestBody(this.pathSpec.getRequestBody());
  }

  /* --- Type parsing functions --- */

  /* This function manage don't manage array, object, anyOf, oneOf, allOf. parseEnum is required for enum parsing
  recursion call */
  private ParameterTypeValidator resolveInnerSchemaPrimitiveTypeValidator(Schema schema, boolean parseEnum) {
    if (schema == null) {
      // It will never reach this
      return ParameterType.GENERIC_STRING.validationMethod();
    }
    if (parseEnum && schema.getEnums() != null && schema.getEnums().size() != 0) {
      return ParameterTypeValidator.createEnumTypeValidatorWithInnerValidator(new ArrayList(schema.getEnums()), this
        .resolveInnerSchemaPrimitiveTypeValidator(schema, false));
    }
    switch (schema.getType()) {
      case "integer":
        if (schema.getFormat() != null && schema.getFormat().equals("int64")) {
          return ParameterTypeValidator.createLongTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() !=
            null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() !=
            null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf
            ().doubleValue() : null, (Long) schema.getDefault() /* TODO test type received */);
        } else {
          return ParameterTypeValidator.createIntegerTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum()
            != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() !=
            null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf
            ().doubleValue() : null, (Integer) schema.getDefault() /* TODO test type received */);
        }
      case "number":
        if (schema.getFormat() != null && schema.getFormat().equals("float"))
          return ParameterTypeValidator.createFloatTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() !=
            null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() !=
            null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf
            ().doubleValue() : null, (Float) schema.getDefault() /* TODO test type received */);
        else
          return ParameterTypeValidator.createDoubleTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum()
            != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() !=
            null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf
            ().doubleValue() : null, (Double) schema.getDefault() /* TODO test type received */);
      case "boolean":
        return ParameterTypeValidator.createBooleanTypeValidator(schema.getDefault());
      case "string":
        String regex = null;
        // Then resolve various string formats
        if (schema.getFormat() != null) switch (schema.getFormat()) {
          case "byte":
            regex = RegularExpressions.BASE64;
          case "date":
            regex = RegularExpressions.DATE;
          case "date-time":
            regex = RegularExpressions.DATETIME;
          case "ipv4":
            regex = RegularExpressions.IPV4;
          case "ipv6":
            regex = RegularExpressions.IPV6;
          case "hostname":
            regex = RegularExpressions.HOSTNAME;
          default:
            throw new SpecFeatureNotSupportedException("format " + schema.getFormat() + " not supported");
        }
        return ParameterTypeValidator.createStringTypeValidator((regex != null) ? regex : schema.getPattern(), schema
          .getMinLength(), schema.getMaxLength(), schema.getDefault());

    }
    return ParameterType.GENERIC_STRING.validationMethod();
  }

  /* This function is an overlay for below function */
  private void resolveObjectTypeFields(ObjectTypeValidator validator, Schema objectSchema) {
    if (objectSchema.getRequiredFields() == null) {
      resolveObjectTypeFields(validator, objectSchema.getProperties(), new ArrayList<>());
    } else {
      resolveObjectTypeFields(validator, objectSchema.getProperties(), new ArrayList<>(objectSchema.getRequiredFields
        ()));
    }
  }

  /* This function resolve object properties type validators */
  private void resolveObjectTypeFields(ObjectTypeValidator validator, Map<String, ? extends Schema> properties,
                                       List<String> requiredFields) {
    for (Map.Entry<String, ? extends Schema> entry : properties.entrySet()) {
      validator.addField(entry.getKey(), this.resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true),
        requiredFields.contains(entry.getKey()));
    }
  }

  /* This function resolve all type validators of anyOf or oneOf type (schema) arrays. It calls the function below */
  private List<ParameterTypeValidator> resolveTypeValidators(List<Schema> schemas, Parameter parent) {
    List<ParameterTypeValidator> result = new ArrayList<>();
    for (Schema schema : schemas) {
      result.add(this.resolveAnyOfOneOfTypeValidator(schema, parent));
    }
    return result;
  }

  /* This function manage a single schema of anyOf or oneOf type (schema) arrays */
  private ParameterTypeValidator resolveAnyOfOneOfTypeValidator(Schema schema, Parameter parent) {
    if (schema.getType().equals("array"))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this
        .resolveInnerSchemaPrimitiveTypeValidator(schema, true), OpenApi3Utils.resolveStyle(parent), parent.isExplode
        (), schema.getMaxItems(), schema.getMinItems());
    else if (schema.getType().equals("object")) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory
        .createObjectTypeValidator(OpenApi3Utils.resolveStyle(parent), parent.isExplode());
      resolveObjectTypeFields(objectTypeValidator, schema);
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaPrimitiveTypeValidator(schema, true);
  }

  /* This function implement logic to create an ObjectTypeValidator from an allOf parameter type */
  private ParameterTypeValidator resolveAllOfParameter(Parameter parameter) {
    Map<String, Schema> properties = new HashMap<>();
    List<String> requiredFields = new ArrayList<>();
    this.resolveAllOfArrays(new ArrayList<Schema>(parameter.getSchema().getAllOfSchemas()), properties, requiredFields);
    ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory
      .createObjectTypeValidator(OpenApi3Utils.resolveStyle(parameter), parameter.isExplode());
    this.resolveObjectTypeFields(objectTypeValidator, properties, requiredFields);
    return objectTypeValidator;
  }

  /* This function resolve all properties inside an allOf array of schemas */
  private void resolveAllOfArrays(List<Schema> allOfSchemas, Map<String, Schema> properties, List<String> requiredFields) {
    for (Schema schema : allOfSchemas) {
      if (schema.getType() != null && !schema.getType().equals("object"))
        throw new SpecFeatureNotSupportedException("allOf only allows inner object types");
      for (Map.Entry<String, ? extends Schema> entry : schema.getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      if (schema.getRequiredFields() != null) requiredFields.addAll(schema.getRequiredFields());
    }
  }

  /* This function check if parameter is of type oneOf, allOf, anyOf and return required type validators. It's
  detached from below function to call it from "magic" workarounds functions */
  private ParameterTypeValidator resolveAnyOfOneOfAllOfTypeValidator(Parameter parameter) {
    if (OpenApi3Utils.isAnyOfSchema(parameter.getSchema())) {
      return new AnyOfTypeValidator(this.resolveTypeValidators(new ArrayList<>(parameter.getSchema().getAnyOfSchemas
        ()), parameter));
    } else if (OpenApi3Utils.isOneOfSchema(parameter.getSchema())) {
      return new OneOfTypeValidator(this.resolveTypeValidators(new ArrayList<>(parameter.getSchema().getOneOfSchemas
        ()), parameter));
    } else if (OpenApi3Utils.isAllOfSchema(parameter.getSchema())) {
      return resolveAllOfParameter(parameter);
    } else return null;
  }

  /* Entry point for resolve type validators */
  private ParameterTypeValidator resolveTypeValidator(Parameter parameter) {
    ParameterTypeValidator candidate = resolveAnyOfOneOfAllOfTypeValidator(parameter);
    if (candidate != null) return candidate;
    else if (OpenApi3Utils.isParameterArrayType(parameter))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this
        .resolveInnerSchemaPrimitiveTypeValidator(parameter.getSchema().getItemsSchema(), true), OpenApi3Utils
        .resolveStyle(parameter), parameter.isExplode(), parameter.getSchema().getMaxItems(), parameter.getSchema()
        .getMinItems());
    else if (OpenApi3Utils.isParameterObjectType(parameter)) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory
        .createObjectTypeValidator(OpenApi3Utils.resolveStyle(parameter), parameter.isExplode());
      resolveObjectTypeFields(objectTypeValidator, parameter.getSchema());
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaPrimitiveTypeValidator(parameter.getSchema(), true);
  }

  /* --- "magic" functions for workarounds (watch below for more info) --- */

  // content field can support every mime type. I will use a default type validator for every content type, except
  // application/json, that i can validate with JsonTypeValidator
  // If content has multiple media types, I use anyOfTypeValidator to support every content type
  private void handleContent(Parameter parameter) {
    Map<String, ? extends MediaType> contents = parameter.getContentMediaTypes();
    ParameterLocation location = resolveLocation(parameter.getIn());
    if (contents.size() == 1 && contents.containsKey("application/json")) {
      this.addRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), JsonTypeValidator.JsonTypeValidatorFactory
          .createJsonTypeValidator(((SchemaImpl) contents.get("application/json").getSchema())
            .getDereferencedJsonTree()), !parameter.getRequired(), (parameter.getAllowEmptyValue() != null) ?
          parameter.getAllowEmptyValue() : false, location), location);
    } else if (contents.size() > 1 && contents.containsKey("application/json")) {
      // Mount anyOf
      List<ParameterTypeValidator> validators = new ArrayList<>();
      validators.add(CONTENT_TYPE_VALIDATOR);
      validators.add(0, JsonTypeValidator.JsonTypeValidatorFactory.createJsonTypeValidator(((SchemaImpl) contents.get
        ("application/json").getSchema()).getDereferencedJsonTree()));
      AnyOfTypeValidator validator = new AnyOfTypeValidator(validators);
      this.addRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), validator, !parameter.getRequired(),
          (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false, location), location);
    } else {
      this.addRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), CONTENT_TYPE_VALIDATOR, !parameter
          .getRequired(), (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
          location), location);
    }
  }

  private void magicParameterExplodedStyleFormTypeObject(Parameter parameter) {
    Map<String, Schema> properties = new HashMap<>();
    List<String> requiredFields = new ArrayList<>();
    if (OpenApi3Utils.isAllOfSchema(parameter.getSchema())) {
      // allOf case
      this.resolveAllOfArrays((List<Schema>) parameter.getSchema().getAllOfSchemas(), properties, requiredFields);
    } else {
      // type object case
      for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      if (parameter.getSchema().getRequiredFields() != null)
        requiredFields.addAll(parameter.getSchema().getRequiredFields());
    }
    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
      if (parameter.getIn().equals("query")) {
        this.addQueryParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
          .createValidationRuleWithCustomTypeValidator(entry.getKey(), new ExpandedObjectFieldValidator(this
            .resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true), parameter.getName(), entry.getKey()), !requiredFields.contains(entry.getKey()), true, ParameterLocation.QUERY));
      } else {
        throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields " +
          "" + "not supported for parameter " + parameter.getName());
      }
    }
  }

  private void magicParameterExplodedStyleSimpleTypeObject(Parameter parameter) {
    Map<String, Schema> properties = new HashMap<>();
    List<String> requiredFields = new ArrayList<>();
    if (OpenApi3Utils.isAllOfSchema(parameter.getSchema())) {
      // allOf case
      this.resolveAllOfArrays((List<Schema>) parameter.getSchema().getAllOfSchemas(), properties, requiredFields);
    } else {
      // type object case
      for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      if (parameter.getSchema().getRequiredFields() != null)
        requiredFields.addAll(parameter.getSchema().getRequiredFields());
    }
    ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory
      .createObjectTypeValidator(ContainerSerializationStyle.simple_exploded_object, false);
    this.resolveObjectTypeFields(objectTypeValidator, properties, requiredFields);
    if (parameter.getIn().equals("path")) {
      this.addPathParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), objectTypeValidator, !OpenApi3Utils
          .isRequiredParam(parameter), (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() :
          false, ParameterLocation.PATH));
    } else if (parameter.getIn().equals("header")) {
      this.addHeaderParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), objectTypeValidator, !OpenApi3Utils
          .isRequiredParam(parameter), (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() :
          false, ParameterLocation.HEADER));
    } else {
      throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields "
        + "not supported for parameter " + parameter.getName());
    }
  }

  private void magicParameterExplodedStyleDeepObjectTypeObject(Parameter parameter) {
    Map<String, Schema> properties = new HashMap<>();
    List<String> requiredFields = new ArrayList<>();
    if (OpenApi3Utils.isAllOfSchema(parameter.getSchema())) {
      // allOf case
      this.resolveAllOfArrays((List<Schema>) parameter.getSchema().getAllOfSchemas(), properties, requiredFields);
    } else {
      // type object case
      for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      if (parameter.getSchema().getRequiredFields() != null)
        requiredFields.addAll(parameter.getSchema().getRequiredFields());
    }
    for (Map.Entry<String, ? extends Schema> entry : properties.entrySet()) {
      if (parameter.getIn().equals("query")) {
        this.addQueryParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
          .createValidationRuleWithCustomTypeValidator(parameter.getName() + "[" + entry.getKey() + "]", new ExpandedObjectFieldValidator(this
            .resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true), parameter.getName(), entry.getKey()), !requiredFields.contains(entry.getKey
            ()), (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false, ParameterLocation.QUERY));
      } else {
        throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields " +
          "" + "not supported for parameter " + parameter.getName());
      }
    }
  }

  /* This function check if a parameter has some particular configurations and run the needed flow to adapt it to
  vertx-web validation framework
   * Included not supported throws:
   * - allowReserved field (it will never be supported)
   * - cookie parameter with explode: true
   * Included workarounds (handled in "magic" functions):
   * - content
   * - exploded: true & style: form & type: object or allOf -> magicParameterExplodedStyleFormTypeObject
   * - exploded: true & style: simple & type: object or allOf -> magicParameterExplodedStyleSimpleTypeObject
   * - exploded: true & style: deepObject & type: object or allOf -> magicParameterExplodedStyleDeepObjectTypeObject
   * */
  private boolean checkSupportedAndNeedWorkaround(Parameter parameter) {
    if (parameter.isAllowReserved()) {
      throw new SpecFeatureNotSupportedException("allowReserved field not supported!");
    } else if (parameter.getContentMediaTypes().size() != 0) {
      handleContent(parameter);
      return true;
    } else /* From this moment only astonishing magic happens */ if (parameter.isExplode()) {
      if (parameter.getIn().equals("cookie")) {
        throw new SpecFeatureNotSupportedException("cookie parameter exploded location not supported");
      } else if (OpenApi3Utils.isParameterStyle(parameter, "form") && (OpenApi3Utils.isParameterObjectOrAllOfType(parameter))) {
        this.magicParameterExplodedStyleFormTypeObject(parameter);
        return true;
      } else if (OpenApi3Utils.isParameterStyle(parameter, "simple") && OpenApi3Utils.isParameterObjectOrAllOfType
        (parameter)) {
        this.magicParameterExplodedStyleSimpleTypeObject(parameter);
        return true;
      } else if (OpenApi3Utils.isParameterStyle(parameter, "deepObject")) {
        this.magicParameterExplodedStyleDeepObjectTypeObject(parameter);
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  /* Function to resolve ParameterLocation from in string */
  private ParameterLocation resolveLocation(String in) {
    switch (in) {
      case "header":
        return ParameterLocation.HEADER;
      case "query":
        return ParameterLocation.QUERY;
      case "cookie":
        return ParameterLocation.COOKIE;
      case "path":
        return ParameterLocation.PATH;
      default:
        throw new SpecFeatureNotSupportedException("in field wrong or not supported");
    }
  }

  /* Entry point for parse Parameter object */
  private void parseParameter(Parameter parameter) {
    if (!checkSupportedAndNeedWorkaround(parameter)) {
      ParameterLocation location = resolveLocation(parameter.getIn());
      this.addRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameter.getName(), this.resolveTypeValidator(parameter),
          !parameter.getRequired(), (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() :
            false, location), location);
    }
  }

  /* --- Request body functions. All functions below are used to parse RequestBody object --- */

  /* This function resolve types for x-www-form-urlencoded. It sets all Collections styles to "csv" */
  private ParameterTypeValidator resolveSchemaTypeValidatorFormEncoded(Schema schema) {
    if (schema.getType().equals("array"))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this
        .resolveInnerSchemaPrimitiveTypeValidator(schema.getItemsSchema(), true), "csv", false, schema.getMaxItems(),
        schema.getMinItems());
    else if (schema.getType().equals("object")) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory
        .createObjectTypeValidator("csv", false);
      resolveObjectTypeFields(objectTypeValidator, schema);
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaPrimitiveTypeValidator(schema, true);
  }

  /* This function resolves default content types of multipart parameters */
  private String resolveDefaultContentTypeRegex(Schema schema) {
    if (schema.getType() != null) {
      if (schema.getType().equals("object")) return Pattern.quote("application/json");
      else if (schema.getType().equals("string") && schema.getFormat() != null && (schema.getFormat().equals
        ("binary") || schema.getFormat().equals("base64")))
        return Pattern.quote("application/octet-stream");
      else if (schema.getType().equals("array")) return this.resolveDefaultContentTypeRegex(schema.getItemsSchema());
      else return Pattern.quote("text/plain");
    }
    throw new SpecFeatureNotSupportedException("Unable to find default content type for multipart parameter. Use " +
      "encoding field");
  }

  /* This function handle all multimaps parameters */
  private void handleMultimapParameter(String parameterName, String contentType, Schema schema, Schema multipartObjectSchema) {
    Pattern contentTypePattern = Pattern.compile(contentType);
    if (contentTypePattern.matcher("application/json").matches()) {
      this.addFormParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameterName, JsonTypeValidator.JsonTypeValidatorFactory
          .createJsonTypeValidator(((SchemaImpl) schema).getDereferencedJsonTree()), !OpenApi3Utils.isRequiredParam
          (multipartObjectSchema, parameterName), false, ParameterLocation.BODY_FORM));
    } else if (contentTypePattern.matcher("text/plain").matches()) {
      this.addFormParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
        .createValidationRuleWithCustomTypeValidator(parameterName, this.resolveSchemaTypeValidatorFormEncoded
          (schema), !OpenApi3Utils.isRequiredParam(multipartObjectSchema, parameterName), false, ParameterLocation
          .BODY_FORM));
    } else {
      this.addCustomValidator(new MultipartCustomValidator(contentTypePattern, parameterName));
    }
  }

  /* Entry point for parse RequestBody object */
  private void parseRequestBody(RequestBody requestBody) {
    if (requestBody != null && requestBody.getContentMediaTypes() != null)
      for (Map.Entry<String, ? extends MediaType> mediaType : requestBody.getContentMediaTypes().entrySet()) {
        if (mediaType.getKey().equals("application/json") && mediaType.getValue().getSchema() != null)
          this.setEntireBodyValidator(JsonTypeValidator.JsonTypeValidatorFactory.createJsonTypeValidator((
            (SchemaImpl) mediaType.getValue().getSchema()).getDereferencedJsonTree()));
        else if (mediaType.getKey().equals("application/x-www-form-urlencoded") && mediaType.getValue().getSchema()
          != null) {
          for (Map.Entry<String, ? extends Schema> paramSchema : mediaType.getValue().getSchema().getProperties()
            .entrySet()) {
            this.addFormParamRule(ParameterValidationRuleImpl.ParameterValidationRuleFactory
              .createValidationRuleWithCustomTypeValidator(paramSchema.getKey(), this
                .resolveSchemaTypeValidatorFormEncoded(paramSchema.getValue()), !OpenApi3Utils.isRequiredParam
                (mediaType.getValue().getSchema(), paramSchema.getKey()), false, ParameterLocation.BODY_FORM));
          }
        } else if (mediaType.getKey().equals("multipart/form-data") && mediaType.getValue().getSchema() != null &&
          mediaType.getValue().getSchema().getType().equals("object")) {
          for (Map.Entry<String, ? extends Schema> multipartProperty : mediaType.getValue().getSchema().getProperties
            ().entrySet()) {
            EncodingProperty encodingProperty = mediaType.getValue().getEncodingProperty(multipartProperty.getKey());
            String contentTypeRegex;
            if (encodingProperty != null && encodingProperty.getContentType() != null)
              contentTypeRegex = OpenApi3Utils.resolveContentTypeRegex(encodingProperty.getContentType());
            else contentTypeRegex = this.resolveDefaultContentTypeRegex(multipartProperty.getValue());
            handleMultimapParameter(multipartProperty.getKey(), contentTypeRegex, multipartProperty.getValue(),
              mediaType.getValue().getSchema());
          }
        } else {
          this.addBodyFileRule(mediaType.getKey());
        }
      }
    if (requestBody.getRequired() == null || !requestBody.getRequired()) this.expectedBodyNotEmpty = false;
  }
}
