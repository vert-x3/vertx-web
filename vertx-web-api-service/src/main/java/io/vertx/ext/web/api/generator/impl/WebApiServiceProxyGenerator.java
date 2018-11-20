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
package io.vertx.ext.web.api.generator.impl;

import io.vertx.codegen.CodeGenProcessor;
import io.vertx.codegen.Generator;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@javax.annotation.processing.SupportedOptions({})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_8)
public class WebApiServiceProxyGenerator extends CodeGenProcessor {

  private static final Predicate<Generator> FILTER = generator ->
    generator.name.contains("web_api_service_proxy_handler") || generator.name.equals("data_object_converters");

  @Override
  protected Predicate<Generator> filterGenerators() {
    return FILTER;
  }
}
