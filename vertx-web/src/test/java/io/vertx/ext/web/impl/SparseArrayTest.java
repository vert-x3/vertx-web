/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.impl;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

public class SparseArrayTest {

  @Test
  public void empty() {
    CapturingConsumer c = new CapturingConsumer();
    SparseArray m = new SparseArray();
    m.clear();
    m.forEachInReverseOrder(c);
    Assert.assertEquals(0, c.acceptedInvocations);
  }

  @Test
  public void one() {
    CapturingConsumer c = new CapturingConsumer();
    SparseArray m = new SparseArray();
    m.clear();
    m.forEachInReverseOrder(c);
    Assert.assertEquals(0, c.acceptedInvocations);
    Object objectSeven = new Object();
    m.put(7, objectSeven);
    m.forEachInReverseOrder(c);
    Assert.assertEquals(1, c.acceptedInvocations);
    Assert.assertEquals(objectSeven, c.accepted.get(0));
  }

  @Test
  public void multi() {
    SparseArray m = new SparseArray();

    Object objectSeven = new Object();
    m.put(7, objectSeven);

    Object objectFour = new Object();
    m.put(4, objectFour);

    Object objectFive = new Object();
    m.put(5, objectFive);

    final Object removed = m.remove(5);
    Assert.assertEquals(objectFive, removed);

    CapturingConsumer c = new CapturingConsumer();
    m.forEachInReverseOrder(c);
    //Two invocations:
    Assert.assertEquals(2, c.acceptedInvocations);
    //In reversed index order:
    Assert.assertEquals(objectSeven, c.accepted.get(0));
    Assert.assertEquals(objectFour, c.accepted.get(1));

    //Can iterate multiple times:
    CapturingConsumer c2 = new CapturingConsumer();
    m.forEachInReverseOrder(c2);
    //Two invocations:
    Assert.assertEquals(2, c2.acceptedInvocations);
    //In reversed index order:
    Assert.assertEquals(objectSeven, c2.accepted.get(0));
    Assert.assertEquals(objectFour, c2.accepted.get(1));

    m.clear();
    //Post-clear iteration:
    CapturingConsumer c3 = new CapturingConsumer();
    m.forEachInReverseOrder(c3);
    Assert.assertEquals(0, c3.acceptedInvocations);
  }

  private static class CapturingConsumer implements Consumer {

    private ArrayList accepted = new ArrayList();
    private int acceptedInvocations = 0;

    @Override
    public void accept(Object o) {
      accepted.add(o);
      acceptedInvocations++;
    }
  }

}
