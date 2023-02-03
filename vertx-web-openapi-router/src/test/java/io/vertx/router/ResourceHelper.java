/*
 * Copyright (c) 2023, SAP SE
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.router;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceHelper {

  public static final Path TEST_RESOURCE_PATH = Paths.get("src", "test", "resources");

  private ResourceHelper() {

  }

  public static Path getRelatedTestResourcePath(Class<?> relatedClass) {
    Path related = Paths.get(relatedClass.getPackage().getName().replace(".", "/"));
    return TEST_RESOURCE_PATH.resolve(related);
  }

  public static JsonObject loadJson(Vertx vertx, Path path) {
    return vertx.fileSystem().readFileBlocking(path.toString()).toJsonObject();
  }
}
