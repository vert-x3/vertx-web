package io.vertx.ext.web.validator;

import io.vertx.core.*;
import io.vertx.ext.web.RoutingContext;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseValidator implements Validator {
  protected class ValidationRule {
    private String name;
    private String regexp;
    private boolean isUnique;
    private boolean isArray;

    public ValidationRule(String name, String regexp, boolean unique, boolean isArray) {
      this.name = name;
      this.regexp = regexp;
      this.isUnique = unique;
      this.isArray = isArray;
    }

    public ValidationRule(String name, String regexp) {
      this.name = name;
      this.regexp = regexp;
    }

    public String getName() {
      return name;
    }

    public String getRegexp() {
      return regexp;
    }

    public boolean isUnique() {
      return isUnique;
    }

    public boolean isArray() {
      return isArray;
    }
  }

  private Map<String, ValidationRule> pathParamsRules;
  private Map<String, ValidationRule> queryParamsRules;
  private Map<String, ValidationRule> formParamsRules;
  private Map<String, ValidationRule> headerParamsRules;
  private String jsonSchema;
  private String xmlSchema;
  private List<CustomValidatorHandler> customValidators;

  @Override
  public Future<Void> validateRequest(RoutingContext routingContext, Handler<RoutingContext> next) {
    Future<Void> mainFuture = Future.future(); // Use future as much as i can for error handling
    //TODO error handling
    try {
      validatePathParams(routingContext.pathParams());
      validateQueryParams(routingContext.queryParams());
      validateHeaderParams(routingContext.request().headers());
    } catch (ValidationException e) {
      mainFuture.tryFail(e);
    }

    //TODO check body content type and:
    // 1. run validation of body content type found
    // 2. if no body content type found, try to run all three validations
    // 3. Discuss the policy with mentor

    if (customValidators != null && customValidators.size() != 0) {
      runCustomValidators(routingContext).setHandler(res -> {
        if (res.succeeded()) {
          mainFuture.tryComplete();
        } else {
          mainFuture.tryFail(res.cause());
        }
      });
    } else {
      mainFuture.complete();
    }
    return mainFuture;
  }

  private void validatePathParams(Map<String, String> pathParams) throws ValidationException {

  }

  private void validateQueryParams(MultiMap queryParams) {

  }

  private void validateFormParams() {

  }

  private void validateHeaderParams(MultiMap headers) {

  }

  private void validateJSONBody() {

  }

  private void validateXMLBody() {

  }

  private Future<Void> runCustomValidators(RoutingContext routingContext) {
    Future<Void> mainFuture = Future.future();
    if (customValidators != null && customValidators.size() != 0) {
      ArrayList<Future> futures = new ArrayList<>();
      for (CustomValidatorHandler customValidator : customValidators) {
        Future fut = Future.future();
        futures.add(customValidator.validate(routingContext));
      }
      CompositeFuture.all(futures).setHandler(res -> {
        if (res.succeeded())
          mainFuture.complete();
        else
          mainFuture.fail(res.cause());
      });
    }
    return mainFuture;
  }

  public void addPathParamRule(String paramName, ValidationRule rule) {
    if (!pathParamsRules.containsKey(paramName))
      pathParamsRules.put(paramName, rule);
    else;
    //TODO what now?
  }

  public void addQueryParamRule(String paramName, ValidationRule rule) {
    if (!queryParamsRules.containsKey(paramName))
      queryParamsRules.put(paramName, rule);
    else;
    //TODO what now?
  }

  public void addFormParamRule(String paramName, ValidationRule rule) {

    if (!formParamsRules.containsKey(paramName))
      formParamsRules.put(paramName, rule);
    else;
    //TODO what now?
  }

  public void addHeaderParamRule(String paramName, ValidationRule rule) {
    if (!headerParamsRules.containsKey(paramName))
      headerParamsRules.put(paramName, rule);
    else;
    //TODO what now?
  }

  public void setJsonSchema(String jsonSchema) {
    if (this.jsonSchema != null)
      this.jsonSchema = jsonSchema;
    else;
      //TODO what now?
  }

  public void setXmlSchema(String xmlSchema) {
    if (this.xmlSchema != null)
      this.xmlSchema = xmlSchema;
    else;
      //TODO what now?
  }
}
