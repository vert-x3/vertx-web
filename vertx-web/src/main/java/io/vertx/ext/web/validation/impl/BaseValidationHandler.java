package io.vertx.ext.web.validation.impl;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.*;

import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseValidationHandler implements ValidationHandler {
  protected class ValidationRule {
    private String name;
    private ParameterTypeValidator validator;
    private boolean isOptional;
    private boolean isArray;

    public ValidationRule(String name, ParameterTypeValidator validator, boolean isOptional, boolean isArray) {
      this.name = name;
      this.validator = validator;
      this.isOptional = isOptional;
      this.isArray = isArray;
    }

    public ValidationRule(String name, ParameterType type, boolean isOptional, boolean isArray) {
      this.name = name;
      this.validator = type.getValidationMethod();
      this.isOptional = isOptional;
      this.isArray = isArray;
    }

    public ValidationRule(String name, String pattern, boolean isOptional, boolean isArray) {
      this.name = name;
      this.validator = new PatternTypeValidator(pattern);
      this.isOptional = isOptional;
      this.isArray = isArray;
    }

    public String getName() {
      return name;
    }

    public ParameterTypeValidator getValidator() {
      return validator;
    }

    public boolean isOptional() {
      return isOptional;
    }

    public boolean isArray() {
      return isArray;
    }

    public boolean validate(String value) {
      if (validator != null)
        return validator.isValid(value);
      else
        return true;
    }
  }

  private Map<String, ValidationRule> pathParamsRules;
  private Map<String, ValidationRule> queryParamsRules;
  private Map<String, ValidationRule> formParamsRules;
  private Map<String, ValidationRule> headerParamsRules;
  private String jsonSchema;
  private String xmlSchema;
  private List<String> fileNamesRules;
  private List<CustomValidator> customValidators;

  protected BaseValidationHandler() {
    pathParamsRules = new HashMap<>();
    formParamsRules = new HashMap<>();
    queryParamsRules = new HashMap<>();
    headerParamsRules = new HashMap<>();
    fileNamesRules = new ArrayList<>();
    customValidators = new ArrayList<>();
  }

  @Override
  public void handle(RoutingContext routingContext) {
    try {
      validatePathParams(routingContext);
      validateQueryParams(routingContext);
      validateHeaderParams(routingContext);

      //Run custom validators
      for (CustomValidator customValidator : customValidators) {
        customValidator.validate(routingContext);
      }

      String contentType = routingContext.request().getHeader("Content-Type");
      if (contentType != null && contentType.length() != 0) {
        if (fileNamesRules.size() != 0 && !contentType.contains("multipart/form-data"))
          throw ValidationException.generateWrongContentTypeExpected(contentType, "multipart/form-data", routingContext);
        if (contentType.contains("application/x-www-form-urlencoded") || contentType.contains("multipart/form-data")) {
          validateFormParams(routingContext);
          if (contentType.contains("multipart/form-data"))
            validateFileUpload(routingContext);
        } else if (contentType.equals("application/json"))
          validateJSONBody();
        else if (contentType.equals("application/xml"))
          validateXMLBody();
        else ;
        //TODO ?!?
      }

      routingContext.next();
    } catch (ValidationException e) {
      routingContext.fail(e);
    }
  }

  private void validatePathParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, String> pathParams = routingContext.pathParams();
    for (ValidationRule rule : pathParamsRules.values()) {
      String name = rule.getName();
      if (pathParams.containsKey(name)) {
        if (!rule.validate(pathParams.get(name)))
          throw ValidationException.generateNotMatchValidationException(name, pathParams.get(name), rule.getValidator(), ParameterLocation.PATH, routingContext);
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.PATH, routingContext);
    }
  }

  private void validateQueryParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap queryParams = routingContext.queryParams();
    for (ValidationRule rule : queryParamsRules.values()) {
      String name = rule.getName();
      if (queryParams.contains(name)) {
        List<String> values = queryParams.getAll(name);
        if (values.size() > 1) {
          if (rule.isArray()) {
            for (String s : values)
              if (!rule.validate(s))
                throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.QUERY, routingContext);
          } else {
            throw ValidationException.generateUnexpectedArrayValidationException(name, ParameterLocation.QUERY, routingContext);
          }
        } else if (!rule.validate(values.get(0)))
          throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.QUERY, routingContext);
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.QUERY, routingContext);
    }
  }

  private void validateHeaderParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap headerParams = routingContext.request().headers();
    for (ValidationRule rule : headerParamsRules.values()) {
      String name = rule.getName();
      if (headerParams.contains(name)) {
        List<String> values = headerParams.getAll(name);
        if (values.size() > 1) {
          if (rule.isArray()) {
            for (String s : values)
              if (!rule.validate(s))
                throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.HEADER, routingContext);
          } else {
            throw ValidationException.generateUnexpectedArrayValidationException(name, ParameterLocation.HEADER, routingContext);
          }
        } else if (!rule.validate(values.get(0)))
          throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.HEADER, routingContext);
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.HEADER, routingContext);
    }
  }

  private void validateFormParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap formParams = routingContext.request().formAttributes();
    for (ValidationRule rule : formParamsRules.values()) {
      String name = rule.getName();
      if (formParams.contains(name)) {
        List<String> values = formParams.getAll(name);
        if (values.size() > 1) {
          if (rule.isArray()) {
            for (String s : values)
              if (!rule.validate(s))
                throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.BODY_FORM, routingContext);
          } else {
            throw ValidationException.generateUnexpectedArrayValidationException(name, ParameterLocation.BODY_FORM, routingContext);
          }
        } else if (!rule.validate(values.get(0)))
          throw ValidationException.generateNotMatchValidationException(name, values.get(0), rule.getValidator(), ParameterLocation.BODY_FORM, routingContext);
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.BODY_FORM, routingContext);
    }
  }

  private boolean existFileUploadName(Set<FileUpload> files, String name) {
    for (FileUpload f : files) {
      if (f.name().equals(name)) return true;
    }
    return false;
  }

  private void validateFileUpload(RoutingContext routingContext) throws ValidationException {
    Set<FileUpload> fileUploads = routingContext.fileUploads();
    for (String expectedFileName : fileNamesRules) {
      if (!existFileUploadName(fileUploads, expectedFileName))
        throw ValidationException.generateFileNotFoundValidationException(expectedFileName, routingContext);
    }
  }

  private void validateJSONBody() {
    //TODO
  }

  private void validateXMLBody() {
    //TODO
  }


  protected void addPathParamRule(ValidationRule rule) {
    if (!pathParamsRules.containsKey(rule.getName()))
      pathParamsRules.put(rule.getName(), rule);
  }

  protected void addQueryParamRule(ValidationRule rule) {
    if (!queryParamsRules.containsKey(rule.getName()))
      queryParamsRules.put(rule.getName(), rule);
  }

  protected void addFormParamRule(ValidationRule rule) {

    if (!formParamsRules.containsKey(rule.getName()))
      formParamsRules.put(rule.getName(), rule);
  }

  protected void addHeaderParamRule(ValidationRule rule) {
    if (!headerParamsRules.containsKey(rule.getName()))
      headerParamsRules.put(rule.getName(), rule);
  }

  protected void addCustomValidator(CustomValidator customValidator) {
    customValidators.add(customValidator);
  }

  protected void addFileUploadName(String formName) {
    fileNamesRules.add(formName);
  }

  protected void setJsonSchema(String jsonSchema) {
    if (this.jsonSchema != null)
      this.jsonSchema = jsonSchema;
  }

  protected void setXmlSchema(String xmlSchema) {
    if (this.xmlSchema != null)
      this.xmlSchema = xmlSchema;
  }
}
