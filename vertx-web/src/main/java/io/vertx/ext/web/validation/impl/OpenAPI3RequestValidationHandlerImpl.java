package io.vertx.ext.web.validation.impl;

import com.reprezen.kaizen.oasparser.model3.*;
import com.reprezen.kaizen.oasparser.ovl3.MediaTypeImpl;
import io.vertx.ext.web.designdriven.OpenApi3Utils;
import io.vertx.ext.web.validation.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RequestValidationHandlerImpl extends HTTPOperationRequestValidationHandlerImpl<Operation> implements io.vertx.ext.web.validation.OpenAPI3RequestValidationHandler {

  public OpenAPI3RequestValidationHandlerImpl(Operation pathSpec) {
    super(pathSpec);
  }

  @Override
  public void parseOperationSpec() {
    // Extract from path spec parameters description
    for (Parameter opParameter : this.pathSpec.getParameters()) {
      this.parseParameter(opParameter);
    }
    this.parseRequestBody(this.pathSpec.getRequestBody());
  }

  private ParameterTypeValidator resolveInnerSchemaTypeValidator(Schema schema, boolean allowedCollection) {
    if (schema.getEnums() != null && schema.getEnums().size() != 0) {
      return ParameterTypeValidator.createEnumTypeValidator(new ArrayList(schema.getEnums()));
    }
    switch (schema.getType()) {
      case "integer":
        if (schema.getFormat() != null && schema.getFormat().equals("int64")) {
          return ParameterTypeValidator.createLongTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null);
        } else {
          return ParameterTypeValidator.createIntegerTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null);
        }
      case "number":
        if (schema.getFormat().equals("float"))
          return ParameterTypeValidator.createFloatTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null);
        else
          return ParameterTypeValidator.createDoubleTypeValidator(schema.isExclusiveMaximum(), (schema.getMaximum() != null) ? schema.getMaximum().doubleValue() : null, schema.isExclusiveMinimum(), (schema.getMinimum() != null) ? schema.getMinimum().doubleValue() : null, (schema.getMultipleOf() != null) ? schema.getMultipleOf().doubleValue() : null);
      case "boolean":
        return ParameterType.BOOL.getValidationMethod();
      case "string":
        // Then resolve various string formats
        if (schema.getFormat() != null)
          switch (schema.getFormat()) {
            case "byte":
              return ParameterType.BASE64.getValidationMethod();
            case "date":
              return ParameterType.DATE.getValidationMethod();
            case "date-time":
              return ParameterType.DATETIME.getValidationMethod();
            case "ipv4":
              return ParameterType.IPV4.getValidationMethod();
            case "ipv6":
              return ParameterType.IPV6.getValidationMethod();
            case "hostname":
              return ParameterType.HOSTNAME.getValidationMethod();
            default:
              throw new SpecFeatureNotSupportedException("format " + schema.getFormat() + " not supported");
          }
        return ParameterTypeValidator.createStringTypeValidator(schema.getPattern(), schema.getMinLength(), schema.getMaxLength());

    }
    return ParameterType.GENERIC_STRING.getValidationMethod();
  }

  private void resolveObjectTypeFields(ObjectTypeValidator validator, Schema objectSchema, boolean allowedCollection) {
    for (Map.Entry<String, ? extends Schema> entry : objectSchema.getProperties().entrySet()) {
      validator.addField(entry.getKey(), this.resolveInnerSchemaTypeValidator(entry.getValue(), allowedCollection), objectSchema.getRequiredFields().contains(entry.getKey()));
    }
  }

  private ParameterTypeValidator resolveTypeValidator(Parameter parameter) {
    if (OpenApi3Utils.isParameterArrayType(parameter))
      return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(this.resolveInnerSchemaTypeValidator(parameter.getSchema().getItemsSchema(), false), parameter.getStyle(), parameter.isExplode(), parameter.getSchema().getMaxItems(), parameter.getSchema().getMinItems());
    else if (OpenApi3Utils.isParameterObjectType(parameter)) {
      ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory.createObjectTypeValidator(parameter.getStyle(), parameter.isExplode());
      resolveObjectTypeFields(objectTypeValidator, parameter.getSchema(), false);
      return objectTypeValidator;
    }
    return this.resolveInnerSchemaTypeValidator(parameter.getSchema(), false);
  }

  private void magicParameterExplodedStyleFormTypeObject(Parameter parameter) {
    for (Map.Entry<String, ? extends Schema> entry : parameter.getSchema().getProperties().entrySet()) {
      if (parameter.getIn().equals("query")) {
        this.addQueryParamRule(
          ParameterValidationRule.createValidationRuleWithCustomTypeValidator(entry.getKey(),
            this.resolveInnerSchemaTypeValidator(entry.getValue(), false),
            !parameter.getSchema().getRequiredFields().contains(entry.getKey()),
            ParameterLocation.QUERY));
      } else if (parameter.getIn().equals("cookie")) {
        // ready for cookie support
      } else {
        throw new SpecFeatureNotSupportedException("combination of style, type and location (in) of parameter fields not supported for parameter " + parameter.getName());
      }
    }
  }

  private void magicParameterExplodedStyleSimpleTypeObject(Parameter parameter) {
    ObjectTypeValidator objectTypeValidator = ObjectTypeValidator.ObjectTypeValidatorFactory.createObjectTypeValidator(ContainerSerializationStyle.simple_exploded_object, false);
    this.resolveObjectTypeFields(objectTypeValidator, parameter.getSchema(), false);
    if (parameter.getIn().equals("path")) {
      this.addPathParamRule(
        ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
          objectTypeValidator,
          !parameter.getRequired(),
          ParameterLocation.PATH));
    } else if (parameter.getIn().equals("header")) {
      this.addHeaderParamRule(
        ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
          objectTypeValidator,
          !parameter.getRequired(),
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
            this.resolveInnerSchemaTypeValidator(entry.getValue(), false),
            !parameter.getSchema().getRequiredFields().contains(entry.getKey()),
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
    } else if (parameter.getIn().equals("cookie")) {
      throw new SpecFeatureNotSupportedException("cookie parameter location not supported");
    } else /* From this moment only astonishing magic happens */ if (parameter.isExplode()) {
      if (OpenApi3Utils.isParameterStyle(parameter, "form") && OpenApi3Utils.isParameterObjectType(parameter)) {
        this.magicParameterExplodedStyleFormTypeObject(parameter);
      } else if (OpenApi3Utils.isParameterStyle(parameter, "simple") && OpenApi3Utils.isParameterObjectType(parameter)) {
        this.magicParameterExplodedStyleSimpleTypeObject(parameter);
      } else if (OpenApi3Utils.isParameterStyle(parameter, "deepObject")) {
        this.magicParameterExplodedStyleDeepObjectTypeObject(parameter);
      }
      return true;
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
            ParameterLocation.HEADER));
          break;
        case "query":
          this.addQueryParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !parameter.getRequired(),
            ParameterLocation.QUERY));
          break;
        case "path":
          this.addPathParamRule(ParameterValidationRule.createValidationRuleWithCustomTypeValidator(parameter.getName(),
            this.resolveTypeValidator(parameter),
            !parameter.getRequired(),
            ParameterLocation.QUERY));
          break;
        case "cookie":
          //TODO talk with mentor
          throw new SpecFeatureNotSupportedException("Parameters in cookie not supported");
      }
    }
  }

  private void parseRequestBody(RequestBody requestBody) {
    MediaType json = requestBody.getContentMediaType("application/json");
    if (json != null) {
      this.setJsonSchema(((MediaTypeImpl) json).getDereferencedJsonTree());
    }
    // TODO add form and multipart
  }
}
