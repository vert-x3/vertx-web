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

package io.vertx.rxjava.serviceproxy.testmodel;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;

/**
 * Test base imports are corrects.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.serviceproxy.testmodel.TestBaseImportsService original} non RX-ified interface using Vert.x codegen.
 */

public class TestBaseImportsService {

  final io.vertx.serviceproxy.testmodel.TestBaseImportsService delegate;

  public TestBaseImportsService(io.vertx.serviceproxy.testmodel.TestBaseImportsService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void m() { 
    this.delegate.m();
  }


  public static TestBaseImportsService newInstance(io.vertx.serviceproxy.testmodel.TestBaseImportsService arg) {
    return arg != null ? new TestBaseImportsService(arg) : null;
  }
}
