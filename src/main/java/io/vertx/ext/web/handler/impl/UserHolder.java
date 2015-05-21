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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

/**
 * Helper class for getting the User object internally in Vert.x-Web
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class UserHolder implements ClusterSerializable {

  public RoutingContext context;
  public Buffer buff;

  public UserHolder() {
  }

  public UserHolder(RoutingContext context) {
    this.context = context;
  }

  @Override
  public void writeToBuffer(Buffer buffer) {
    User user = context.user();
    if (user != null) {
      if (user instanceof ClusterSerializable) {
        ClusterSerializable cs = (ClusterSerializable)user;
        Buffer buff = Buffer.buffer();
        cs.writeToBuffer(buff);
        buffer.appendInt(buff.length());
        buffer.appendBuffer(buff);
        cs.writeToBuffer(buffer);
      } else {
        throw new IllegalArgumentException("User is not a ClusterSerializable");
      }
    } else {
      buffer.appendInt(0);
    }
  }

  @Override
  public int readFromBuffer(int pos, Buffer buffer) {
    int len = buffer.getInt(pos);
    pos += 4;
    if (len != 0) {
      buff = buffer.getBuffer(pos, pos + len);
      pos += len;
    } else {
      buff = null;
    }
    return pos;
  }
}
