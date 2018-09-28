package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class XMLTypeValidator implements ParameterTypeValidator {

  private Validator schemaValidator;

  private XMLTypeValidator(Validator schemaValidator) throws SAXNotRecognizedException, SAXNotSupportedException {
    this.schemaValidator = schemaValidator;
    // protect this validator against XXE
    this.schemaValidator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    this.schemaValidator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    try {
      DocumentBuilder parser = createDocumentBuilderFactoryInstance().newDocumentBuilder();
      Document document = parser.parse(value);
      this.schemaValidator.validate(new DOMSource(document));
      return RequestParameter.create(document);
    } catch (Exception e) {
      throw ValidationException.ValidationExceptionFactory.generateInvalidXMLBodyException(e.getMessage());
    }
  }

  public static class XMLTypeValidatorFactory {
    public static XMLTypeValidator createXMLTypeValidator(String xmlSchema) {
      try {
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = createSchemaFactoryInstance();
        // load a WXS schema, represented by a Schema instance
        Source xmlSchemaSource = new StreamSource(new StringReader(xmlSchema));
        return new XMLTypeValidator(factory.newSchema(xmlSchemaSource).newValidator());
      } catch (SAXException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Safely create a DocumentBuilderFactory following OWASP best practises
   * @return DocumentBuilderFactory instance
   */
  private static DocumentBuilderFactory createDocumentBuilderFactoryInstance() throws ParserConfigurationException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    String FEATURE;

    // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
    // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
    FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    dbf.setFeature(FEATURE, true);

    // If you can't completely disable DTDs, then at least do the following:
    // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
    // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
    // JDK7+ - http://xml.org/sax/features/external-general-entities
    FEATURE = "http://xml.org/sax/features/external-general-entities";
    dbf.setFeature(FEATURE, false);

    // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
    // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
    // JDK7+ - http://xml.org/sax/features/external-parameter-entities
    FEATURE = "http://xml.org/sax/features/external-parameter-entities";
    dbf.setFeature(FEATURE, false);

    // Disable external DTDs as well
    FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    dbf.setFeature(FEATURE, false);

    // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);

    // And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
    // ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
    // (http://cwe.mitre.org/data/definitions/918.html) and denial
    // of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."

    return dbf;
  }

  /**
   * Creates a SchemaFactory instance following OWASP best practices
   * @return SchemaFactory instance
   */
  private static SchemaFactory createSchemaFactoryInstance() throws SAXNotRecognizedException, SAXNotSupportedException {
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    return factory;
  }
}
