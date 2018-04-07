package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.AttributeFormDataPart;
import io.vertx.ext.web.client.impl.FileUploadFormDataPart;

/**
 * A form data part of the request body.
 */
@VertxGen
public interface FormDataPart {
  /**
   * Create a form data part of an attribute.
   *
   * @param key   the key of the attribute
   * @param value the value of the attribute
   * @return the form data part
   */
  static FormDataPart createAttribute(String key, String value) {
    return new AttributeFormDataPart(key, value);
  }

  /**
   * Create a form data part to upload a file.
   *
   * @param name      name of the parameter
   * @param filename  filename of the file
   * @param pathname  the pathname of this file
   * @param mediaType the MIME type of this file
   * @param isText    true when this file should be transmitted in text format(else binary)
   * @return the form data part
   */
  static FormDataPart createFileUpload(String name, String filename, String pathname, String mediaType, boolean isText) {
    return new FileUploadFormDataPart(name, filename, pathname, mediaType, isText);
  }
}
