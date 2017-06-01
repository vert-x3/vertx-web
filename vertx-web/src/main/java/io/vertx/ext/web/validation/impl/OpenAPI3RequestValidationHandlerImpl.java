package io.vertx.ext.web.validation.impl;

import com.reprezen.kaizen.oasparser.model3.*;
import com.reprezen.kaizen.oasparser.ovl3.MediaTypeImpl;
import io.vertx.ext.web.designdriven.OpenApi3Utils;
import io.vertx.ext.web.validation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RequestValidationHandlerImpl extends HTTPOperationRequestValidationHandlerImpl<Operation> implements io.vertx.ext.web.validation.OpenAPI3RequestValidationHandler {

  List<Parameter> parentParams;

  public OpenAPI3RequestValidationHandlerImpl(Operation pathSpec, List<Parameter> parentParams) {
    super(pathSpec);
    if (parentParams == null)
      this.parentParams = new ArrayList<>();
    else
      this.parentParams = parentParams;
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
          if (!(parentParam.getIn().equalsIgnoreCase(actualParam.getIn()) && parentParam.getName().equals(actualParam.getName())))
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

  private ParameterTypeValidator resolveSchemaTypeValidatorFormEncoded(Schema schema) {
    if (schema.getType().equals("array"))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this.resolveInnerSchemaPrimitiveTypeValidator(schema.getItemsSchema(), true), "csv", false, schema.getMaxItems(), schema.getMinItems());
    else if (schema.getType().equals("object")) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory.createObjectTypeValidator("csv", false);
      resolveObjectTypeFields(objectTypeValidator, schema);
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaPrimitiveTypeValidator(schema, true);
  }

  private ParameterTypeValidator resolveInnerSchemaPrimitiveTypeValidator(Schema schema, boolean parseEnum) {
    if (schema == null) {
      // It will never reach this
      return ParameterType.GENERIC_STRING.getValidationMethod();
    }
    if (parseEnum && schema.getEnums() != null && schema.getEnums().size() != 0) {
      return ParameterTypeValidator.createEnumTypeValidator(new ArrayList(schema.getEnums()), this.resolveInnerSchemaPrimitiveTypeValidator(schema, false));
    }
    switch (schema.getType()) {
      case "integer":
        if (schema.getFormat() != null && schema.getFormat().equals("int64")) {
          return ParameterTypeValidator.createLongTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null, (Long) schema.getDefault() /* TODO test type received */);
        } else {
          return ParameterTypeValidator.createIntegerTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null, (Integer) schema.getDefault() /* TODO test type received */);
        }
      case "number":
        if (schema.getFormat() != null && schema.getFormat().equals("float"))
          return ParameterTypeValidator.createFloatTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null, (Float) schema.getDefault() /* TODO test type received */);
        else
          return ParameterTypeValidator.createDoubleTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null, (Double) schema.getDefault() /* TODO test type received */);
      case "boolean":
        return ParameterTypeValidator.createBooleanTypeValidator(schema.getDefault());
      case "string":
        String regex = null;
        // Then resolve various string formats
        if (schema.getFormat() != null)
          switch (schema.getFormat()) {
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
        return ParameterTypeValidator.createStringTypeValidator((regex != null) ? regex : schema.getPattern(), schema.getMinLength(), schema.getMaxLength(), schema.getDefault());

    }
    return ParameterType.GENERIC_STRING.getValidationMethod();
  }

  private void resolveObjectTypeFields(ObjectTypeValidator validator, Schema objectSchema) {
    for (Map.Entry<String, ? extends Schema> entry : objectSchema.getProperties().entrySet()) {
      validator.addField(entry.getKey(), this.resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true), objectSchema.getRequiredFields().contains(entry.getKey()));
    }
  }

  private ParameterTypeValidator resolveTypeValidator(Parameter parameter) {
    if (OpenApi3Utils.isParameterArrayType(parameter))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this.resolveInnerSchemaPrimitiveTypeValidator(parameter.getSchema().getItemsSchema(), true), OpenApi3Utils.resolveStyle(parameter), parameter.isExplode(), parameter.getSchema().getMaxItems(), parameter.getSchema().getMinItems());
    else if (OpenApi3Utils.isParameterObjectType(parameter)) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory.createObjectTypeValidator(OpenApi3Utils.resolveStyle(parameter), parameter.isExplode());
      resolveObjectTypeFields(objectTypeValidator, parameter.getSchema());
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaPrimitiveTypeValidator(parameter.getSchema(), true);
  }

  private void magicParameterExplodedStyleFormTypeObject(Parameter parameter) {
    for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
      if (parameter.getIn().equals("query")) {
        this.addQueryParamRule(
          ParameterValidationRule.createValidationRuleWithCustomTypeValidator(entry.getKey(),
            this.resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true),
            !OpenApi3Utils.isRequiredParam(parameter.getSchema(), entry.getKey()),
            true,
            ParameterLocation.QUERY));
      } else {
        throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields not supported for parameter " + parameter.getName());
      }
    }
  }

  private void magicParameterExplodedStyleSimpleTypeObject(Parameter parameter) {
    ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory.createObjectTypeValidator(ContainerSerializationStyle.simple_exploded_object, false);
    this.resolveObjectTypeFields(objectTypeValidator, parameter.getSchema());
    if (parameter.getIn().equals("path")) {
      this.addPathParamRule(
        ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
          objectTypeValidator,
          !OpenApi3Utils.isRequiredParam(parameter),
          (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
          ParameterLocation.PATH));
    } else if (parameter.getIn().equals("header")) {
      this.addHeaderParamRule(
        ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
          objectTypeValidator,
          !OpenApi3Utils.isRequiredParam(parameter),
          (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
          ParameterLocation.HEADER));
    } else {
      throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields not supported for parameter " + parameter.getName());
    }
  }

  private void magicParameterExplodedStyleDeepObjectTypeObject(Parameter parameter) {
    for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
      if (parameter.getIn().equals("query")) {
        this.addQueryParamRule(
          ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName() + "[" + entry.getKey() + "]",
            this.resolveInnerSchemaPrimitiveTypeValidator(entry.getValue(), true),
            !OpenApi3Utils.isRequiredParam(parameter.getSchema(), entry.getKey()),
            true,
            ParameterLocation.QUERY));
      } else {
        throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields not supported for parameter " + parameter.getName());
      }
    }
  }

  // This function check if a parameter has some particular configurations and run the needed flow to adapt it to vertx-web validation framework
  private boolean checkSupportedAndNeedWorkaround(Parameter parameter) {
    if (parameter.isAllowReserved()) {
      throw new SpecFeatureNotSupportedException("allowReserved not supported for parameter " + parameter.getName());
    } else if (parameter.getContentMediaTypes().size() != 0) {
      throw new SpecFeatureNotSupportedException("content not supported for parameter " + parameter.getName());
    } else if ((parameter.getSchema().getAllOfSchemas() != null && parameter.getSchema().getAllOfSchemas().size() != 0) ||
      (parameter.getSchema().getAnyOfSchemas() != null && parameter.getSchema().getAnyOfSchemas().size() != 0)) {
      throw new SpecFeatureNotSupportedException("anyOf, oneOf, allOf not supported for parameter " + parameter.getName());
    } else /* From this moment only astonishing magic happens */ if (parameter.isExplode()) {
      if (parameter.getIn().equals("cookie")) {
        throw new SpecFeatureNotSupportedException("cookie parameter exploded location not supported");
      } else if (OpenApi3Utils.isParameterStyle(parameter, "form") && OpenApi3Utils.isParameterObjectType(parameter)) {
        this.magicParameterExplodedStyleFormTypeObject(parameter);
        return true;
      } else if (OpenApi3Utils.isParameterStyle(parameter, "simple") && OpenApi3Utils.isParameterObjectType(parameter)) {
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

  private void parseParameter(Parameter parameter) {
    if (!checkSupportedAndNeedWorkaround(parameter)) {
      switch (parameter.getIn()) {
        case "header":
          this.addHeaderParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !parameter.getRequired(),
            (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
            ParameterLocation.HEADER));
          break;
        case "query":
          this.addQueryParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !OpenApi3Utils.isRequiredParam(parameter),
            (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
            ParameterLocation.QUERY));
          break;
        case "path":
          this.addPathParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !OpenApi3Utils.isRequiredParam(parameter),
            (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
            ParameterLocation.PATH));
          break;
        case "cookie":
          this.addCookieParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !OpenApi3Utils.isRequiredParam(parameter),
            (parameter.getAllowEmptyValue() != null) ? parameter.getAllowEmptyValue() : false,
            ParameterLocation.COOKIE));
          break;
      }
    }
  }

  private void parseRequestBody(RequestBody requestBody) {
    MediaType json = requestBody.getContentMediaType("application/json");
    if (json != null) {
      this.setEntireBodyValidator(JsonTypeValidator.JsonTypeValidatorFactory.createJsonTypeValidator(((MediaTypeImpl) json).getDereferencedJsonTree().get("schema")));
    }

    MediaType formUrlEncoded = requestBody.getContentMediaType("x-www-form-urlencoded");
    if (formUrlEncoded != null && formUrlEncoded.getSchema() != null) {
      for (Map.Entry<String, ? extends Schema> paramSchema : formUrlEncoded.getSchema().getProperties().entrySet()) {
        this.addFormParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(paramSchema.getKey(),
          this.resolveSchemaTypeValidatorFormEncoded(paramSchema.getValue()),
          !OpenApi3Utils.isRequiredParam(paramSchema.getValue(), paramSchema.getKey()),
          false,
          ParameterLocation.BODY_FORM));
      }
    }

    MediaType multipart = requestBody.getContentMediaType("multipart/form-data");
    if (multipart != null && multipart.getSchema() != null) {
      throw new SpecFeatureNotSupportedException("multipart not supported");
    }
  }
}
