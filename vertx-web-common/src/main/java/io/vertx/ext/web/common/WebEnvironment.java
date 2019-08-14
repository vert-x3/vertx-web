/*
 * Copyright 2018 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.common;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Utility API to verify which environment is the web application running.
 *
 * The utility will check initially for the existence of a system property under the name `vertx.mode`,
 * if there is no such property then it will look under the environment variables under the name `VERTX_MODE`.
 *
 * This value will be then used when the API is invoked. By itself this utility will not
 * affect the behavior of your application, however you can use it to simplify your handlers, e.g.:
 *
 * When the development mode is active you can log more information or disable caches.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface WebEnvironment {

  String SYSTEM_PROPERTY_NAME = "vertxweb.environment";
  String ENV_VARIABLE_NAME = "VERTXWEB_ENVIRONMENT";

  /**
   * Will return true if the mode is not null and equals ignoring case the string "dev"
   * @return always boolean
   */
  static boolean development() {
    final String mode = mode();
    return "dev".equalsIgnoreCase(mode) || "Development".equalsIgnoreCase(mode);
  }

  /**
   * The current mode from the system properties with fallback to environment variables
   * @return String with mode value or null
   */
  static @Nullable String mode() {
    return System.getProperty(SYSTEM_PROPERTY_NAME, System.getenv(ENV_VARIABLE_NAME));
  }
}
