/*
 * Copyright 2023 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.impl;

import io.vertx.core.Handler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HandlersCollectionTest {

  private final HandlersCollection<Object> handlersCollection = new HandlersCollection<>();
  private int counter = 0;

  @Test
  public void one() {
    CapturingHandler ch = new CapturingHandler();
    handlersCollection.put(ch);
    Object event = new Object();
    handlersCollection.invokeInReverseOrder(event);
    assertEquals(1, ch.events.size());
    assertEquals(event, ch.events.get(0));
  }

  @Test
  public void removeOutOfBounds() {
    handlersCollection.put(new CapturingHandler());
    assertFalse(handlersCollection.remove(-1));
    assertFalse(handlersCollection.remove(1));
  }

  @Test
  public void multi() {
    CapturingHandler ch1 = new CapturingHandler();
    handlersCollection.put(ch1);

    CapturingHandler ch2 = new CapturingHandler();
    int id2 = handlersCollection.put(ch2);

    CapturingHandler ch3 = new CapturingHandler();
    handlersCollection.put(ch3);

    assertTrue(handlersCollection.remove(id2));
    assertFalse(handlersCollection.remove(id2));

    Object event1 = new Object();
    handlersCollection.invokeInReverseOrder(event1);

    assertEquals(0, ch3.index);
    assertEquals(1, ch3.events.size());
    assertEquals(event1, ch3.events.get(0));

    assertEquals(0, ch2.events.size());

    assertEquals(1, ch1.index);
    assertEquals(1, ch1.events.size());
    assertEquals(event1, ch1.events.get(0));

    Object event2 = new Object();
    handlersCollection.invokeInReverseOrder(event2);

    assertEquals(2, ch3.index);
    assertEquals(2, ch3.events.size());
    assertEquals(event2, ch3.events.get(1));

    assertEquals(0, ch2.events.size());

    assertEquals(3, ch1.index);
    assertEquals(2, ch1.events.size());
    assertEquals(event2, ch1.events.get(1));

    handlersCollection.clear();
    handlersCollection.invokeInReverseOrder(new Object());

    assertEquals(2, ch3.events.size());
    assertEquals(0, ch2.events.size());
    assertEquals(2, ch1.events.size());
  }

  @Test
  public void deleteOnEmpty() {
    assertFalse(handlersCollection.remove(3));
  }

  private class CapturingHandler implements Handler<Object> {

    List<Object> events = new ArrayList<>();
    int index;

    @Override
    public void handle(Object event) {
      events.add(event);
      index = counter++;
    }
  }
}
