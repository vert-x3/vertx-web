package io.vertx.ext.web.api.validation.impl;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class XMLTypeValidatorTest {

  @Test
  public void isValidPattern() throws IOException {

      File tempFile = File.createTempFile("xml-", "-xxe");
      tempFile.deleteOnExit();

    try (OutputStream out = new FileOutputStream(tempFile)) {
      out.write("<appinfo>you should not see me!!!</appinfo>".getBytes(StandardCharsets.UTF_8));
    }

    try {
      XMLTypeValidator.XMLTypeValidatorFactory.createXMLTypeValidator(
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
          "<!DOCTYPE foo [ <!ELEMENT foo ANY >\n" +
          "<!ENTITY xxe SYSTEM \"file://" + tempFile.getCanonicalPath() + "\" >]>\n" +
          "<creds>\n" +
          "    <user>&xxe;</user>\n" +
          "    <pass>mypass</pass>\n" +
          "</creds>"
      );
    } catch (Throwable e) {
      if (e.getCause() instanceof SAXParseException) {
        SAXParseException xxe = (SAXParseException) e.getCause();
        // here comes the nasty XXE verification
        if (xxe.getSystemId() == null && xxe.getMessage().contains("Failed to read external document")) {
          // we're safe, the parsed failed to load the XXE
        } else {
          fail("XML got access to FS: " + xxe.getMessage());
        }
      }
    }

  }
}
