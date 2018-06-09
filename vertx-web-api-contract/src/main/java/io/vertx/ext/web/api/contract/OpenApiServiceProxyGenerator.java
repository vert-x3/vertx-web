/*
 * Copyright 2014 Red Hat, Inc.
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
package io.vertx.ext.web.api.contract;

import io.vertx.codegen.CodeGenProcessor;
import io.vertx.codegen.Generator;

import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@javax.annotation.processing.SupportedOptions({})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_8)
public class OpenApiServiceProxyGenerator extends CodeGenProcessor {

  private static final Predicate<Generator> FILTER = generator ->
    generator.name.contains("openapi_service_proxies") || generator.name.equals("data_object_converters");

  @Override
  protected Predicate<Generator> filterGenerators() {
    return FILTER;
  }
}
