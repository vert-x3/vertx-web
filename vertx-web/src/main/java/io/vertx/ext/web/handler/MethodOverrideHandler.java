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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.impl.MethodOverrideHandlerImpl;

/**
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
@VertxGen
public interface MethodOverrideHandler extends PlatformHandler {
  /**
   * Create a X-HTTP-METHOD-OVERRIDE handler with safe downgrade of methods
   *
   * @return the X-HTTP-METHOD-OVERRIDE handler
   */
  static MethodOverrideHandler create() { return new MethodOverrideHandlerImpl(); }

  /**
   * Create a X-HTTP-METHOD-OVERRIDE handler
   *
   * @param useSafeDowngrade if set to true, the method overriding will not happen if the overridden method is more
   *                         idempotent or safer than the overriding method.
   * @return the X-HTTP-METHOD-OVERRIDE handler
   */
  static MethodOverrideHandler create(boolean useSafeDowngrade) {
    return new MethodOverrideHandlerImpl(useSafeDowngrade);
  }
}
