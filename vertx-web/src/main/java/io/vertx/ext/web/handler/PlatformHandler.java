/*
 * Copyright 2021 Red Hat, Inc.
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
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Base platform interface for handlers that provide functionality to the application platform.
 *
 * Two examples are:
 *
 * <ul>
 *   <li>{@link LoggerHandler}</li>
 *   <li>{@link MethodOverrideHandler}</li>
 *   <li>{@link SessionHandler}</li>
 * </ul>
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen(concrete = false)
public interface PlatformHandler extends Handler<RoutingContext> {
}
