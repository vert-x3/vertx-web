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

package io.vertx.groovy.serviceproxy.testmodel;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
*/
@CompileStatic
public class TestConnection {
  private final def io.vertx.serviceproxy.testmodel.TestConnection delegate;
  public TestConnection(Object delegate) {
    this.delegate = (io.vertx.serviceproxy.testmodel.TestConnection) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public TestConnection startTransaction(Handler<AsyncResult<String>> resultHandler) {
    delegate.startTransaction(resultHandler);
    return this;
  }
  public TestConnection insert(String name, Map<String, Object> data, Handler<AsyncResult<String>> resultHandler) {
    delegate.insert(name, data != null ? new io.vertx.core.json.JsonObject(data) : null, resultHandler);
    return this;
  }
  public TestConnection commit(Handler<AsyncResult<String>> resultHandler) {
    delegate.commit(resultHandler);
    return this;
  }
  public TestConnection rollback(Handler<AsyncResult<String>> resultHandler) {
    delegate.rollback(resultHandler);
    return this;
  }
  public void close() {
    delegate.close();
  }
}
