/*
 * Copyright (c) 2011-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
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
  static FormDataPart attribute(String key, String value) {
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
  static FormDataPart fileUpload(String name, String filename, String pathname, String mediaType, boolean isText) {
    return new FileUploadFormDataPart(name, filename, pathname, mediaType, isText);
  }
}
