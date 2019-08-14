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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A form data part of a {@link MultipartForm}.
 */
@VertxGen
public interface FormDataPart {

  /**
   * @return the name
   */
  @CacheReturn
  String name();

  /**
   * @return {@code true} when this part is an attribute
   */
  @CacheReturn
  boolean isAttribute();

  /**
   * @return {@code true} when this part is a file upload
   */
  @CacheReturn
  boolean isFileUpload();

  /**
   * @return the value when the part for a form attribute otherwise {@code null}
   */
  @CacheReturn
  String value();

  /**
   * @return the filename when this part is a file upload otherwise {@code null}
   */
  @CacheReturn
  String filename();

  /**
   * @return the pathname when this part is a file upload otherwise {@code null}
   */
  @CacheReturn
  String pathname();

  /**
   * @return the media type when this part is a file upload otherwise {@code null}
   */
  @CacheReturn
  String mediaType();

  /**
   * @return whether the file upload is text or binary when this part is a file upload otherwise {@code null}
   */
  @CacheReturn
  Boolean isText();

}
