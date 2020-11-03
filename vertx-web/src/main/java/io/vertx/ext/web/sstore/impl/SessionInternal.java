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
package io.vertx.ext.web.sstore.impl;

/**
 * Internal interface to allow cleanup of internal state after a session is stored.
 */
public interface SessionInternal {

  /**
   * Mark this session as flushed, this gives the object a change to clear any state management flags.
   *
   * @param skipCrc if the intention is NOT to keep using the session after this call,
   *                a small optimization can be performed (skip updating the internal CRC)
   *                which is unnecessary.
   */
  void flushed(boolean skipCrc);
}
