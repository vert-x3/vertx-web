/*
 * Copyright 2019 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.templ.pebble.impl;

import io.pebbletemplates.pebble.attributes.AttributeResolver;
import io.pebbletemplates.pebble.extension.AbstractExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicola Murino <nicola dot murino at gmail.com>
 */

class PebbleVertxExtension extends AbstractExtension {

  @Override
  public List<AttributeResolver> getAttributeResolver() {

    List<AttributeResolver> attributeResolvers = new ArrayList<>();
    attributeResolvers.add(new PebbleVertxAttributeResolver());
    return attributeResolvers;
  }

}
