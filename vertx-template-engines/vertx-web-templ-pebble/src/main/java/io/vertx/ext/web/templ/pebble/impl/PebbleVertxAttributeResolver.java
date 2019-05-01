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

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.attributes.ResolvedAttribute;
import com.mitchellbosecke.pebble.error.AttributeNotFoundException;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Nicola Murino <nicola dot murino at gmail.com> 
 */

public class PebbleVertxAttributeResolver implements AttributeResolver {

  @Override
  public ResolvedAttribute resolve(Object instance, Object attributeNameValue, Object[] argumentValues,
      ArgumentsNode args, EvaluationContextImpl context, String filename, int lineNumber) {

    if (instance instanceof JsonObject) {
      ResolvedAttribute resolvedAttribute = new ResolvedAttribute(null);

      if (attributeNameValue != null && attributeNameValue instanceof String) {
        JsonObject jsonObject = (JsonObject) instance;
        resolvedAttribute = new ResolvedAttribute(jsonObject.getValue((String) attributeNameValue));
      }
      if (context.isStrictVariables() && resolvedAttribute.evaluatedValue == null) {
        throw new AttributeNotFoundException(null,
            String.format(
                "Attribute [%s] of [%s] does not exist or can not be accessed and strict variables is set to true.",
                attributeNameValue.toString(), instance.getClass().getName()),
            attributeNameValue.toString(), lineNumber, filename);
      }

      return resolvedAttribute;
    } else if (instance instanceof JsonArray) {
      JsonArray jsonArray = (JsonArray) instance;
      String attributeName = String.valueOf(attributeNameValue);
      int index;
      try {
        index = Integer.parseInt(attributeName);
      } catch (NumberFormatException e) {
        return null;
      }
      int length = jsonArray.size();

      if (index < 0 || index >= length) {
        if (context.isStrictVariables()) {
          throw new AttributeNotFoundException(null,
              "Index out of bounds while accessing JsonArray with strict variables on.", attributeName, lineNumber,
              filename);
        } else {
          return new ResolvedAttribute(null);
        }
      }

      return new ResolvedAttribute(jsonArray.getValue(index));
    }

    return null;
  }

}
