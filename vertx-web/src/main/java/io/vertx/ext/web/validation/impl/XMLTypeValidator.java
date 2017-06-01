package io.vertx.ext.web.validation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.impl.RequestParameterImpl;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class XMLTypeValidator implements ParameterTypeValidator {

  private Validator schemaValidator;

  private XMLTypeValidator(Validator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    try {
      DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = parser.parse(value);
      this.schemaValidator.validate(new DOMSource(document));
      return RequestParameter.create(document);
    } catch (Exception e) {
      throw ValidationException.generateInvalidXMLBodyException(e.getMessage());
    }
  }

  public static class XMLTypeValidatorFactory {
    public static XMLTypeValidator createXMLTypeValidator(String xmlSchema) {
      // create a SchemaFactory capable of understanding WXS schemas
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // load a WXS schema, represented by a Schema instance
      Source xmlSchemaSource = new StreamSource(new StringReader(xmlSchema));
      try {
        return new XMLTypeValidator(factory.newSchema(xmlSchemaSource).newValidator());
      } catch (SAXException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

}
