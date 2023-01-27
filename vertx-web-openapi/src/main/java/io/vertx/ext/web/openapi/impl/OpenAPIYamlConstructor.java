package io.vertx.ext.web.openapi.impl;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Date;

public class OpenAPIYamlConstructor extends SafeConstructor {

  public OpenAPIYamlConstructor() {
    super();
    this.yamlConstructors.put(Tag.TIMESTAMP, new ConstructInstantTimestamp());
  }

  private static class ConstructInstantTimestamp extends SafeConstructor.ConstructYamlTimestamp {
    public Object construct(Node node) {
      Date date = (Date) super.construct(node);
      return date.toInstant();
    }
  }
}
