/*
 * Copyright 2023 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.impl;

import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;

class HandlersCollection<E> {

  private final List<Handler<E>> list;

  HandlersCollection() {
    this.list = new ArrayList<>();
  }

  int put(Handler<E> handler) {
    list.add(handler);
    return list.size() - 1;
  }

  boolean remove(int handlerID) {
    if (handlerID < 0 || handlerID >= list.size()) {
      return false;
    }
    return list.set(handlerID, null) != null;
  }

  void clear() {
    list.clear();
  }

  void invokeInReverseOrder(E event) {
    for (int i = list.size() - 1; i >= 0; i--) {
      Handler<E> handler = list.get(i);
      if (handler != null) {
        handler.handle(event);
      }
    }
  }
}
