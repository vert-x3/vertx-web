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
package io.vertx.ext.web.codec.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * The body stream decodes a {@link WriteStream<Buffer>} into the a {@code T} instance.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface BodyStream<T> extends WriteStream<Buffer>, Handler<Throwable> {

  /**
   * @return the future signaling the completion of the stream
   */
  Future<T> result();

}
