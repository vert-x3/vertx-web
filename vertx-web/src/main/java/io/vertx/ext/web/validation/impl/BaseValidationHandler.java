package io.vertx.ext.web.validation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class BaseValidationHandler implements ValidationHandler {

  private Map<String, ParameterValidationRule> pathParamsRules;
  private Map<String, ParameterValidationRule> queryParamsRules;
  private Map<String, ParameterValidationRule> formParamsRules;
  private Map<String, ParameterValidationRule> headerParamsRules;
  private JsonSchema jsonSchema;
  private Validator xmlSchemaValidator;
  private List<String> fileNamesRules;
  private List<CustomValidator> customValidators;

  private boolean expectedBodyNotEmpty;

  protected BaseValidationHandler() {
    pathParamsRules = new HashMap<>();
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
          throw ValidationException.generateWrongContentTypeExpected(contentType, "multipart/form-data");
        if (contentType.contains("application/x-www-form-urlencoded") || contentType.contains("multipart/form-data")) {
          validateFormParams(routingContext);
          if (contentType.contains("multipart/form-data"))
            validateFileUpload(routingContext);
        } else if (contentType.equals("application/json"))
          validateJSONBody(routingContext);
        else if (contentType.equals("application/xml"))
          validateXMLBody(routingContext);
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
      routingContext.next();
    } catch (ValidationException e) {
      routingContext.fail(e);
    }
  }

  private void validatePathParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, String> pathParams = routingContext.pathParams();
    for (ParameterValidationRule rule : pathParamsRules.values()) {
      String name = rule.getName();
      if (pathParams.containsKey(name)) {
        rule.validateSingleParam(pathParams.get(name));
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.PATH);
    }
  }

  private void validateQueryParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap queryParams = routingContext.queryParams();
    for (ParameterValidationRule rule : queryParamsRules.values()) {
      String name = rule.getName();
      if (queryParams.contains(name)) {
        rule.validateArrayParam(queryParams.getAll(name));
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.QUERY);
    }
  }

  private void validateHeaderParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap headersParams = routingContext.request().headers();
    for (ParameterValidationRule rule : headerParamsRules.values()) {
      String name = rule.getName();
      if (headersParams.contains(name)) {
        rule.validateArrayParam(headersParams.getAll(name));
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.HEADER);
    }
  }

  private void validateFormParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    MultiMap formParams = routingContext.request().formAttributes();
    for (ParameterValidationRule rule : formParamsRules.values()) {
      String name = rule.getName();
      if (formParams.contains(name)) {
        rule.validateArrayParam(formParams.getAll(name));
      } else if (!rule.isOptional())
        throw ValidationException.generateNotFoundValidationException(name, ParameterLocation.BODY_FORM);
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
        throw ValidationException.generateFileNotFoundValidationException(expectedFileName);
    }
  }

  private void validateJSONBody(RoutingContext routingContext) throws ValidationException {
    if (jsonSchema != null) {
      try {
        Set<ValidationMessage> errors = jsonSchema.validate(new ObjectMapper().readTree(routingContext.getBodyAsString()));
        if (!errors.isEmpty())
          ValidationException.generateInvalidJsonBodyException(errors.toString());
      } catch (IOException e) {
        throw ValidationException.generateNotParsableJsonBodyException();
      }
    }
  }

  private void validateXMLBody(RoutingContext routingContext) throws ValidationException {
    if (xmlSchemaValidator != null) {
      try {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(routingContext.getBodyAsString());
        this.xmlSchemaValidator.validate(new DOMSource(document));
      } catch (Exception e) {
        throw ValidationException.generateInvalidXMLBodyException(e.getMessage());
      }
    }
  }


  protected void addPathParamRule(ParameterValidationRule rule) {
    if (!pathParamsRules.containsKey(rule.getName()))
      pathParamsRules.put(rule.getName(), rule);
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

  protected void setJsonSchema(String jsonSchema) {
    if (this.jsonSchema != null) {
      try {
        this.jsonSchema = new JsonSchemaFactory().getSchema(new ObjectMapper().readTree(jsonSchema));
      } catch (IOException e) {
        e.printStackTrace();
      }
      expectedBodyNotEmpty = true;
    }
  }

  protected void setJsonSchema(JsonNode jsonSchema) {
    if (this.jsonSchema != null) {
      this.jsonSchema = new JsonSchemaFactory().getSchema(jsonSchema);
      expectedBodyNotEmpty = true;
    }
  }

  protected void setXmlSchema(String xmlSchema) {
    if (xmlSchema != null) {
      // create a SchemaFactory capable of understanding WXS schemas
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // load a WXS schema, represented by a Schema instance
      Source xmlSchemaSource = new StreamSource(new StringReader(xmlSchema));
      try {
        this.xmlSchemaValidator = factory.newSchema(xmlSchemaSource).newValidator();
        expectedBodyNotEmpty = true;
      } catch (SAXException e) {
        e.printStackTrace();
      }
    }
  }
}
