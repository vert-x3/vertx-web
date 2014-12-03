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
import io.vertx.groovy.core.buffer.Buffer
import java.util.Set
import io.vertx.core.json.JsonObject
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Bodies {
  final def io.vertx.ext.apex.addons.Bodies delegate;
  public Bodies(io.vertx.ext.apex.addons.Bodies delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Bodies bodies() {
    def ret= Bodies.FACTORY.apply(io.vertx.ext.apex.addons.Bodies.bodies());
    return ret;
  }
  public static Bodies bodies(long bodyLimit) {
    def ret= Bodies.FACTORY.apply(io.vertx.ext.apex.addons.Bodies.bodies(bodyLimit));
    return ret;
  }
  public static Bodies bodies(String uploadsDirectory) {
    def ret= Bodies.FACTORY.apply(io.vertx.ext.apex.addons.Bodies.bodies(uploadsDirectory));
    return ret;
  }
  public static Bodies bodies(long bodyLimit, String uploadsDirectory) {
    def ret= Bodies.FACTORY.apply(io.vertx.ext.apex.addons.Bodies.bodies(bodyLimit, uploadsDirectory));
    return ret;
  }
  public static String getBodyAsString() {
    def ret = io.vertx.ext.apex.addons.Bodies.getBodyAsString();
    return ret;
  }
  public static String getBodyAsString(String encoding) {
    def ret = io.vertx.ext.apex.addons.Bodies.getBodyAsString(encoding);
    return ret;
  }
  public static Map<String, Object> getBodyAsJson() {
    def ret = io.vertx.ext.apex.addons.Bodies.getBodyAsJson()?.getMap();
    return ret;
  }
  public static Buffer getBody() {
    def ret= Buffer.FACTORY.apply(io.vertx.ext.apex.addons.Bodies.getBody());
    return ret;
  }
  public static Set<FileUpload> fileUploads() {
    def ret = io.vertx.ext.apex.addons.Bodies.fileUploads()?.collect({underpants -> FileUpload.FACTORY.apply(underpants)}) as Set;
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.Bodies, Bodies> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.Bodies arg -> new Bodies(arg);
  };
}
