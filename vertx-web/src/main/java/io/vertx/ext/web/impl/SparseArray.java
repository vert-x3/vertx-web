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

import java.util.Arrays;

/**
 * Internal container which replaces the need for allocating
 * expensive instances of TreeMap(s) of Integer,Handler tuples,
 * which were being used for the sole purpose of reverse iteration
 * on the registered handlers.
 * This should be slightly more efficient in terms of memory
 * pressure, including during the iteration process.
 * Please do not reuse: this was specifically designed for a
 * specific purpose and assumes a very dense data set.
 *
 * @param <H>
 * @author Sanne Grinovero
 */
final class SparseArray<H> {

  private Object[] elements;

  void forEachInReverseOrder(final java.util.function.Consumer<H> action) {
    if (elements != null) {
      for (int i = elements.length - 1; i >= 0; i--) {
        final Object element = elements[i];
        if (element != null) {
          action.accept((H) element);
        }
      }
    }
  }

  void clear() {
    if (elements!=null) {
      Arrays.fill(elements, null);
    }
  }

  void put(final int seq, final H handler) {
    if (elements == null || seq >= elements.length) {
      resizeToFit(seq);
    }
    elements[seq] = handler;
  }

  private void resizeToFit(final int seq) {
    final int existingLength = elements == null ? 0 : elements.length;
    //2,4,8.. seems like a reasonable scaling sequence for this use case:
    //not many elements are expected, on the other hand we don't want to have
    //to re-size the array frequently when we lose this bet.
    //But also, always make sure to reach at least the value of seq.
    if (existingLength != 0) {
      final int targetLength = Math.max((seq + 1), (existingLength * 2));
      final Object[] newArray = new Object[targetLength];
      System.arraycopy(elements, 0, newArray, 0, existingLength);
      elements = newArray;
    } else {
      final int targetLength = Math.max((seq + 1), 2);
      elements = new Object[targetLength];
    }
  }

  H remove(final int handlerID) {
    if (handlerID < elements.length) {
      final H removed = (H) elements[handlerID];
      elements[handlerID] = null;
      return removed;
    } else {
      return null;
    }
  }

}
