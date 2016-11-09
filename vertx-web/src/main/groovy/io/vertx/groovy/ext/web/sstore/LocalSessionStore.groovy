/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.web.sstore;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.Session
import io.vertx.groovy.core.Vertx
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * A session store which is only available on a single node.
 * <p>
 * Can be used when sticky sessions are being used.
*/
@CompileStatic
public class LocalSessionStore extends SessionStore {
  private final def io.vertx.ext.web.sstore.LocalSessionStore delegate;
  public LocalSessionStore(Object delegate) {
    super((io.vertx.ext.web.sstore.LocalSessionStore) delegate);
    this.delegate = (io.vertx.ext.web.sstore.LocalSessionStore) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a session store
   * @param vertx the Vert.x instance
   * @return the session store
   */
  public static LocalSessionStore create(Vertx vertx) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.sstore.LocalSessionStore.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null), io.vertx.groovy.ext.web.sstore.LocalSessionStore.class);
    return ret;
  }
  /**
   * Create a session store
   * @param vertx the Vert.x instance
   * @param sessionMapName name for map used to store sessions
   * @return the session store
   */
  public static LocalSessionStore create(Vertx vertx, String sessionMapName) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.sstore.LocalSessionStore.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null, sessionMapName), io.vertx.groovy.ext.web.sstore.LocalSessionStore.class);
    return ret;
  }
  /**
   * Create a session store
   * @param vertx the Vert.x instance
   * @param sessionMapName name for map used to store sessions
   * @param reaperInterval how often, in ms, to check for expired sessions
   * @return the session store
   */
  public static LocalSessionStore create(Vertx vertx, String sessionMapName, long reaperInterval) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.sstore.LocalSessionStore.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null, sessionMapName, reaperInterval), io.vertx.groovy.ext.web.sstore.LocalSessionStore.class);
    return ret;
  }
}
