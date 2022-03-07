/*
 * Copyright 2022 Red Hat, Inc.
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
package io.vertx.ext.web.impl;

/**
 * Internal interface to listed for the order added to the router. All Handlers that also implement this interface will
 * receive a callback {@link #onOrder(int)} when the handler is added to the router.
 *
 * This information can be useful for handlers that define dependencies on other handlers/routes so the order can be
 * propagated downstream.
 *
 * @author Paulo Lopes
 */
public interface OrderListener {

  /**
   * Called only once, when an implementation is added to a router and the order is known. Updates to the router will
   * not propagate the event.
   *
   * @param order the order number.
   */
  void onOrder(int order);
}
