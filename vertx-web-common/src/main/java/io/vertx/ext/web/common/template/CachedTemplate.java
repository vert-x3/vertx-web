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
package io.vertx.ext.web.common.template;

import io.vertx.core.shareddata.Shareable;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CachedTemplate<T> implements Shareable {

  private final T template;
  private final String baseDir;

  public CachedTemplate(T template) {
    this(template, null);
  }

  public CachedTemplate(T template, String baseDir) {
    this.template = template;
    this.baseDir = baseDir;
  }

  public T template() {
    return template;
  }

  public String baseDir() {
    return baseDir;
  }
}
