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

package io.vertx.groovy.ext.apex.core;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Session {
  final def io.vertx.ext.apex.core.Session delegate;
  public Session(io.vertx.ext.apex.core.Session delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public Map<String, Object> data() {
    def ret = this.delegate.data()?.getMap();
    return ret;
  }
  public String id() {
    def ret = this.delegate.id();
    return ret;
  }
  public long lastAccessed() {
    def ret = this.delegate.lastAccessed();
    return ret;
  }
  public void setAccessed() {
    this.delegate.setAccessed();
  }
  public void destroy() {
    this.delegate.destroy();
  }
  public boolean isDestroyed() {
    def ret = this.delegate.isDestroyed();
    return ret;
  }
  public long timeout() {
    def ret = this.delegate.timeout();
    return ret;
  }
  public SessionStore sessionStore() {
    def ret= SessionStore.FACTORY.apply(this.delegate.sessionStore());
    return ret;
  }
  public boolean isLoggedIn() {
    def ret = this.delegate.isLoggedIn();
    return ret;
  }
  public void logout() {
    this.delegate.logout();
  }
  public void setPrincipal(String principal) {
    this.delegate.setPrincipal(principal);
  }
  public String getPrincipal() {
    def ret = this.delegate.getPrincipal();
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.Session, Session> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.Session arg -> new Session(arg);
  };
}
