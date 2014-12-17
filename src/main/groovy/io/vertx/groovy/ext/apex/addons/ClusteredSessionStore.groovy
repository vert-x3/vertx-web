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

package io.vertx.groovy.ext.apex.addons;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.core.SessionStore
import io.vertx.groovy.core.Vertx
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class ClusteredSessionStore extends SessionStore {
  final def io.vertx.ext.apex.addons.ClusteredSessionStore delegate;
  public ClusteredSessionStore(io.vertx.ext.apex.addons.ClusteredSessionStore delegate) {
    super(delegate);
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static SessionStore clusteredSessionStore(Vertx vertx, String sessionMapName) {
    def ret= SessionStore.FACTORY.apply(io.vertx.ext.apex.addons.ClusteredSessionStore.clusteredSessionStore((io.vertx.core.Vertx)vertx.getDelegate(), sessionMapName));
    return ret;
  }
  public static SessionStore clusteredSessionStore(Vertx vertx) {
    def ret= SessionStore.FACTORY.apply(io.vertx.ext.apex.addons.ClusteredSessionStore.clusteredSessionStore((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.ClusteredSessionStore, ClusteredSessionStore> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.ClusteredSessionStore arg -> new ClusteredSessionStore(arg);
  };
}
