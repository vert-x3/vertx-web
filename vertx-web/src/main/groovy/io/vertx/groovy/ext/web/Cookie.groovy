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
public class Cookie {
  private final def io.vertx.ext.web.Cookie delegate;
  public Cookie(Object delegate) {
    this.delegate = (io.vertx.ext.web.Cookie) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Cookie cookie(String name, String value) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.Cookie.cookie(name, value), io.vertx.groovy.ext.web.Cookie.class);
    return ret;
  }
  public String getName() {
    def ret = delegate.getName();
    return ret;
  }
  public String getValue() {
    def ret = delegate.getValue();
    return ret;
  }
  public Cookie setValue(String arg0) {
    delegate.setValue(arg0);
    return this;
  }
  public Cookie setDomain(String arg0) {
    delegate.setDomain(arg0);
    return this;
  }
  public String getDomain() {
    def ret = delegate.getDomain();
    return ret;
  }
  public Cookie setPath(String arg0) {
    delegate.setPath(arg0);
    return this;
  }
  public String getPath() {
    def ret = delegate.getPath();
    return ret;
  }
  public Cookie setMaxAge(long arg0) {
    delegate.setMaxAge(arg0);
    return this;
  }
  public Cookie setSecure(boolean arg0) {
    delegate.setSecure(arg0);
    return this;
  }
  public Cookie setHttpOnly(boolean arg0) {
    delegate.setHttpOnly(arg0);
    return this;
  }
  public String encode() {
    def ret = delegate.encode();
    return ret;
  }
  public boolean isChanged() {
    def ret = delegate.isChanged();
    return ret;
  }
  public void setChanged(boolean arg0) {
    delegate.setChanged(arg0);
  }
}
