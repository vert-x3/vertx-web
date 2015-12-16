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

package serviceproxy.testmodel;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
*/
@CompileStatic
public class TestConnectionWithCloseFuture {
  private final def serviceproxy.testmodel.TestConnectionWithCloseFuture delegate;
  public TestConnectionWithCloseFuture(Object delegate) {
    this.delegate = (serviceproxy.testmodel.TestConnectionWithCloseFuture) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void close(Handler<AsyncResult<Void>> handler) {
    this.delegate.close(handler);
  }
  public void someMethod(Handler<AsyncResult<String>> resultHandler) {
    this.delegate.someMethod(resultHandler);
  }
}
