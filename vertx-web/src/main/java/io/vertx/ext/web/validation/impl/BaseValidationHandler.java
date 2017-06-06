package io.vertx.ext.web.validation.impl;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RequestParameterImpl;
import io.vertx.ext.web.impl.RequestParametersImpl;
import io.vertx.ext.web.validation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseValidationHandler implements ValidationHandler {

  private Map<String, ParameterValidationRule> pathParamsRules;
  private Map<String, ParameterValidationRule> cookieParamsRules;
  private Map<String, ParameterValidationRule> queryParamsRules;
  private Map<String, ParameterValidationRule> formParamsRules;
  private Map<String, ParameterValidationRule> headerParamsRules;
  private ParameterTypeValidator entireBodyValidator;
  private List<String> fileNamesRules;
  private List<CustomValidator> customValidators;

  private boolean expectedBodyNotEmpty;

  protected BaseValidationHandler() {
    pathParamsRules = new HashMap<>();
    cookieParamsRules = new HashMap<>();
    formParamsRules = new HashMap<>();
    queryParamsRules = new HashMap<>();
    headerParamsRules = new HashMap<>();
    fileNamesRules = new ArrayList<>();
    customValidators = new ArrayList<>();

    expectedBodyNotEmpty = false;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    try {
      RequestParametersImpl parsedParameters = new RequestParametersImpl();

      parsedParameters.setPathParameters(validatePathParams(routingContext));
      parsedParameters.setQueryParameters(validateQueryParams(routingContext));
      parsedParameters.setHeaderParameters(validateHeaderParams(routingContext));
      parsedParameters.setCookieParameters(validateCookieParams(routingContext));

      //Run custom validators
      for (CustomValidator customValidator : customValidators) {
        customValidator.validate(routingContext);
      }

      String contentType = routingContext.request().getHeader("Content-Type");
      if (contentType != null && contentType.length() != 0) {
        if (fileNamesRules.size() != 0 && !contentType.contains("multipart/form-data"))
          throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(contentType, "multipart/form-data");
        if (contentType.contains("application/x-www-form-urlencoded") || contentType.contains("multipart/form-data")) {
          parsedParameters.setFormParameters(validateFormParams(routingContext));
          if (contentType.contains("multipart/form-data"))
            validateFileUpload(routingContext);
        } else if (contentType.equals("application/json") || contentType.equals("application/xml"))
          parsedParameters.setBody(validateEntireBody(routingContext));
        else {
          routingContext.fail(400);
          return;
        }
      } else {
        if (expectedBodyNotEmpty) {
          routingContext.fail(400);
          return;
        }
      }

      routingContext.put("parsedParameters", parsedParameters);

      routingContext.next();
    } catch (ValidationException e) {
      routingContext.fail(e);
    }
  }

  private Map<String, RequestParameter> validatePathParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    Map<String, String> pathParams = routingContext.pathParams();
    for (ParameterValidationRule rule : pathParamsRules.values()) {
      String name = rule.getName();
      if (pathParams.containsKey(name)) {
        RequestParameter parsedParam = rule.validateSingleParam(pathParams.get(name));
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else // Path params are required!
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name, ParameterLocation.PATH);
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateCookieParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    List<Cookie> cookies = new ArrayList<>(routingContext.cookies());
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    for (ParameterValidationRule rule : cookieParamsRules.values()) {
      String name = rule.getName();
      boolean resolved = false;
      for (int i = 0; i < cookies.size(); i++) {
        if (cookies.get(i).getName().equals(name)) {
          RequestParameter parsedParam = rule.validateSingleParam(cookies.get(i).getValue());
          parsedParams.put(parsedParam.getName(), parsedParam);
          cookies.remove(i);
          resolved = true;
          break;
        }
      }
      if (!resolved) {
        if (rule.allowEmptyValue() && rule.getParameterTypeValidator().getDefault() != null) {
          RequestParameter parsedParam = new RequestParameterImpl(name, rule.getParameterTypeValidator().getDefault());
          parsedParams.put(parsedParam.getName(), parsedParam);
        } else if (!rule.isOptional())
          throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name, ParameterLocation.COOKIE);
      }
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateQueryParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap queryParams = routingContext.queryParams();
    for (ParameterValidationRule rule : queryParamsRules.values()) {
      String name = rule.getName();
      if (queryParams.contains(name)) {
        RequestParameter parsedParam = rule.validateArrayParam(queryParams.getAll(name));
        parsedParam.setName(name);
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (rule.allowEmptyValue() && rule.getParameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.getParameterTypeValidator().getDefault());
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name, ParameterLocation.QUERY);
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateHeaderParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap headersParams = routingContext.request().headers();
    for (ParameterValidationRule rule : headerParamsRules.values()) {
      String name = rule.getName();
      if (headersParams.contains(name)) {
        RequestParameter parsedParam = rule.validateArrayParam(headersParams.getAll(name));
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (rule.allowEmptyValue() && rule.getParameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.getParameterTypeValidator().getDefault());
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name, ParameterLocation.HEADER);
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateFormParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap formParams = routingContext.request().formAttributes();
    for (ParameterValidationRule rule : formParamsRules.values()) {
      String name = rule.getName();
      if (formParams.contains(name)) {
        // Decode values
        List<String> values = new ArrayList<>();
        for (String s : formParams.getAll(name)) {
          try {
            values.add(URLDecoder.decode(s, "UTF-8"));
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        }
        RequestParameter parsedParam = rule.validateArrayParam(values);
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (rule.allowEmptyValue() && rule.getParameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.getParameterTypeValidator().getDefault());
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name, ParameterLocation.BODY_FORM);
    }
    return parsedParams;
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
        throw ValidationException.ValidationExceptionFactory.generateFileNotFoundValidationException(expectedFileName);
    }
  }

  private RequestParameter validateEntireBody(RoutingContext routingContext) throws ValidationException {
    if (entireBodyValidator != null)
      return entireBodyValidator.isValid(routingContext.getBodyAsString());
    else
      return RequestParameter.create(null);
  }

  protected void addRule(ParameterValidationRule rule, ParameterLocation location) {
    switch (location) {
      case PATH:
        addPathParamRule(rule);
        break;
      case HEADER:
        addHeaderParamRule(rule);
        break;
      case COOKIE:
        addCookieParamRule(rule);
        break;
      case QUERY:
        addQueryParamRule(rule);
        break;
      case BODY_FORM:
        addFormParamRule(rule);
        break;
    }
  }

  protected void addPathParamRule(ParameterValidationRule rule) {
    if (!pathParamsRules.containsKey(rule.getName()))
      pathParamsRules.put(rule.getName(), rule);
  }

  protected void addCookieParamRule(ParameterValidationRule rule) {
    if (!cookieParamsRules.containsKey(rule.getName()))
      cookieParamsRules.put(rule.getName(), rule);
  }

  protected void addQueryParamRule(ParameterValidationRule rule) {
    if (!queryParamsRules.containsKey(rule.getName()))
      queryParamsRules.put(rule.getName(), rule);
  }

  protected void addFormParamRule(ParameterValidationRule rule) {
    if (!formParamsRules.containsKey(rule.getName())) {
      formParamsRules.put(rule.getName(), rule);
      expectedBodyNotEmpty = true;
    }
  }

  protected void addHeaderParamRule(ParameterValidationRule rule) {
    if (!headerParamsRules.containsKey(rule.getName()))
      headerParamsRules.put(rule.getName(), rule);
  }

  protected void addCustomValidator(CustomValidator customValidator) {
    customValidators.add(customValidator);
  }

  protected void addFileUploadName(String formName) {
    fileNamesRules.add(formName);
    expectedBodyNotEmpty = true;
  }

  protected void setEntireBodyValidator(ParameterTypeValidator entireBodyValidator) {
    this.entireBodyValidator = entireBodyValidator;
    expectedBodyNotEmpty = true;
  }
}
