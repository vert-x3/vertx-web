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

/**
 * A specialized collection of handlers for {@link RoutingContextImpl}.
 * <p>
 * This collection is not thread-safe because it is assumed the handlers are set when executing on the context bound to the request.
 * <p>
 * The underlying list can grow to accomodate new handlers and removal doesn't trigger compaction.
 * This is required because the index of the handler in the list is used as its unique id.
 * This isn't a problem as long as the appplication doesn't add dozens of handlers for each event (headers end, body end, response end).
 */
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
