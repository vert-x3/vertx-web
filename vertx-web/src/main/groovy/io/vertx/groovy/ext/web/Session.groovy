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

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
@CompileStatic
public class Session {
  private final def io.vertx.ext.web.Session delegate;
  public Session(Object delegate) {
    this.delegate = (io.vertx.ext.web.Session) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public String id() {
    def ret = delegate.id();
    return ret;
  }
  public Session put(String arg0, Object arg1) {
    delegate.put(arg0, arg1 != null ? InternalHelper.unwrapObject(arg1) : null);
    return this;
  }
  public <T> T get(String arg0) {
    def ret = (T) InternalHelper.wrapObject(delegate.get(arg0));
    return ret;
  }
  public <T> T remove(String arg0) {
    def ret = (T) InternalHelper.wrapObject(delegate.remove(arg0));
    return ret;
  }
  public long lastAccessed() {
    def ret = delegate.lastAccessed();
    return ret;
  }
  public void destroy() {
    delegate.destroy();
  }
  public boolean isDestroyed() {
    def ret = delegate.isDestroyed();
    return ret;
  }
  public long timeout() {
    def ret = delegate.timeout();
    return ret;
  }
  public void setAccessed() {
    delegate.setAccessed();
  }
}
