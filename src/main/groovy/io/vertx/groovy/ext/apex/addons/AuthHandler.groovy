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
import io.vertx.groovy.ext.apex.core.RoutingContext
import java.util.Set
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class AuthHandler {
  final def io.vertx.ext.apex.addons.AuthHandler delegate;
  public AuthHandler(io.vertx.ext.apex.addons.AuthHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public AuthHandler addRole(String role) {
    def ret= AuthHandler.FACTORY.apply(this.delegate.addRole(role));
    return ret;
  }
  public AuthHandler addPermission(String permission) {
    def ret= AuthHandler.FACTORY.apply(this.delegate.addPermission(permission));
    return ret;
  }
  public AuthHandler addRoles(Set<String> roles) {
    def ret= AuthHandler.FACTORY.apply(this.delegate.addRoles(roles));
    return ret;
  }
  public AuthHandler addPermissions(Set<String> permissions) {
    def ret= AuthHandler.FACTORY.apply(this.delegate.addPermissions(permissions));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.AuthHandler, AuthHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.AuthHandler arg -> new AuthHandler(arg);
  };
}
