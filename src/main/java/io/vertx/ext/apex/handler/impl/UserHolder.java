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

package io.vertx.ext.apex.handler.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * Helper class for getting the User object internally in Apex
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class UserHolder implements ClusterSerializable {

  static final String SESSION_USER_KEY = "__vertx.user";

  static User getUser(AuthProvider authProvider, RoutingContext context) {
    // First look on the context
    User user = context.user();
    if (user != null) {
      return user;
    } else {
      // It may be serialized in the session
      Session session = context.session();
      if (session != null) {
        UserHolder holder = session.get(SESSION_USER_KEY);
        if (holder != null) {
          user = holder.getUser0(authProvider, context);
          if (user != null) {
            context.setUser(user);
            return user;
          } else {
            // routingcontext user set to null on last route
            session.remove(SESSION_USER_KEY);
          }
        }
      }
      return null;
    }
  }

  static void setUser(RoutingContext context, User user) {
    context.setUser(user);
    Session session = context.session();
    if (session != null && user.isClusterable()) {
      session.put(SESSION_USER_KEY, new UserHolder(context));
    }
  }

  private RoutingContext context;
  private Buffer buff;

  public UserHolder() {
  }

  public UserHolder(RoutingContext context) {
    this.context = context;
  }

  User getUser0(AuthProvider provider, RoutingContext newContext) {
    try {
      if (buff != null) {
        User user = provider.fromBuffer(buff);
        buff = null;
        return user;
      } else if (context != null) {
        return context.user();
      } else {
        return null;
      }
    } finally {
      this.context = newContext;
    }
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
