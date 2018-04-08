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
package io.vertx.ext.web.multipart;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.multipart.impl.MultipartFormImpl;

/**
 * A multipart form.
 */
@VertxGen
public interface MultipartForm extends Iterable<FormDataPart> {

  /**
   * @return a multipart form instance
   */
  static MultipartForm create() {
    return new MultipartFormImpl();
  }

  /**
   * Add an attribute form data part.
   *
   * @param name  the name of the attribute
   * @param value the value of the attribute
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MultipartForm attribute(String name, String value);

  /**
   * Add a file upload form data part.
   *
   * @param name      name of the parameter
   * @param filename  filename of the file
   * @param pathname  the pathname of the file
   * @param mediaType the MIME type of the file
   * @param isText    true when this file should be transmitted in text format (else binary)
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MultipartForm fileUpload(String name, String filename, String pathname, String mediaType, boolean isText);

}
