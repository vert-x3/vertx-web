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
import io.vertx.ext.web.common.WebEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:andy@mazebert.com">Andreas Hager</a>
 */
public class VertxDirectoryCodeResolver implements CodeResolver {
  private final Vertx vertx;
  private final Path templateRootDirectory;
  private final ConcurrentMap<String, Long> modificationTimes;

  public VertxDirectoryCodeResolver(Vertx vertx, String templateRootDirectory) {
    this.vertx = vertx;
    this.templateRootDirectory = Paths.get(templateRootDirectory);

    if (WebEnvironment.development()) {
      modificationTimes = new ConcurrentHashMap<>();
    } else {
      modificationTimes = null;
    }
  }

  public String resolve(String name) {
    name = templateRootDirectory.resolve(name).toString();

    String templateCode = vertx.fileSystem().readFileBlocking(name).toString();
    if (templateCode == null) {
      return null;
    }

    if (modificationTimes != null) {
      modificationTimes.put(name, this.getLastModified(name));
    }

    return templateCode;
  }

  public boolean hasChanged(String name) {
    if (modificationTimes == null) {
      return false;
    }

    name = templateRootDirectory.resolve(name).toString();

    Long lastResolveTime = this.modificationTimes.get(name);
    if (lastResolveTime == null) {
      return true;
    } else {
      long lastModified = this.getLastModified(name);
      return lastModified != lastResolveTime;
    }
  }

  private long getLastModified(String name) {
    return vertx.fileSystem().propsBlocking(name).lastModifiedTime();
  }

  public void clear() {
    if (modificationTimes != null) {
      modificationTimes.clear();
    }
  }
}
