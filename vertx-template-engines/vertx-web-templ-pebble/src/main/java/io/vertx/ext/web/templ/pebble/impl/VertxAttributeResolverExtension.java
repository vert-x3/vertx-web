package io.vertx.ext.web.templ.pebble.impl;

import java.util.ArrayList;
import java.util.List;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.extension.AbstractExtension;

public class VertxAttributeResolverExtension extends AbstractExtension {

  @Override
  public List<AttributeResolver> getAttributeResolver() {
    List<AttributeResolver> attributeResolvers = new ArrayList<>();
    attributeResolvers.add(new JsonObjectAttributeResolver());
    attributeResolvers.add(new JsonArrayAttributeResolver());
    return attributeResolvers;
  }
}
