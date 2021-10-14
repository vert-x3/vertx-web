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

package io.vertx.ext.web.templ.jte.impl;

import gg.jte.CodeResolver;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:andy@mazebert.com">Andreas Hager</a>
 */
public class VertxDirectoryCodeResolver implements CodeResolver {
  private final FileSystem fs;
  private final Path templateRootDirectory;

  public VertxDirectoryCodeResolver(Vertx vertx, String templateRootDirectory) {
    this.fs = vertx.fileSystem();
    this.templateRootDirectory = Paths.get(templateRootDirectory);
  }

  @Override
  public String resolve(String name) {
    return fs
      .readFileBlocking(templateRootDirectory.resolve(name).toString())
      .toString();
  }

  @Override
  public long getLastModified(String name) {
    return fs
      .propsBlocking(templateRootDirectory.resolve(name).toString())
      .lastModifiedTime();
  }
}
