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
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class StaticServer {
  final def io.vertx.ext.apex.addons.StaticServer delegate;
  public StaticServer(io.vertx.ext.apex.addons.StaticServer delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static StaticServer staticServer(String root) {
    def ret= StaticServer.FACTORY.apply(io.vertx.ext.apex.addons.StaticServer.staticServer(root));
    return ret;
  }
  public static StaticServer staticServer() {
    def ret= StaticServer.FACTORY.apply(io.vertx.ext.apex.addons.StaticServer.staticServer());
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)event.getDelegate());
  }
  public StaticServer setWebRoot(String webRoot) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setWebRoot(webRoot));
    return ret;
  }
  public StaticServer setFilesReadOnly(boolean readOnly) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setFilesReadOnly(readOnly));
    return ret;
  }
  public StaticServer setMaxAgeSeconds(long maxAgeSeconds) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setMaxAgeSeconds(maxAgeSeconds));
    return ret;
  }
  public StaticServer setCachingEnabled(boolean enabled) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setCachingEnabled(enabled));
    return ret;
  }
  public StaticServer setDirectoryListing(boolean directoryListing) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setDirectoryListing(directoryListing));
    return ret;
  }
  public StaticServer setIncludeHidden(boolean includeHidden) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setIncludeHidden(includeHidden));
    return ret;
  }
  public StaticServer setCacheEntryTimeout(long timeout) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setCacheEntryTimeout(timeout));
    return ret;
  }
  public StaticServer setIndexPage(String indexPage) {
    def ret= StaticServer.FACTORY.apply(this.delegate.setIndexPage(indexPage));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.StaticServer, StaticServer> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.StaticServer arg -> new StaticServer(arg);
  };
}
