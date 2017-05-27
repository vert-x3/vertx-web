package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.StandardContainerDeserializer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ContainerSerializationStyle {
  /**
   * <a href="https://tools.ietf.org/html/rfc6570#section-3.2.7">RFC6570 Path style parameters</a>
   */
  //TODO talk with mentor about that
  /*rfc6570_path_style_parameter_expansion("", new ContainerDeserializer() {
    @Override
    public List<String> deserializeArray(String serialized, boolean exploded) {
      ArrayList<String> list = new ArrayList<>();
      if (exploded) {

      } else {
        serialized = serialized.trim();
        if (serialized.charAt(0) == '.')
          serialized = serialized.substring(1);
        return Arrays.asList(serialized.split(","));
      }
      return list;
    }

    @Override
    public Map<String, String> deserializeObject(String serialized, boolean exploded) {
      return null;
    }
  }),*/

  csv(new StandardContainerDeserializer(","));
  //TODO implement other types


  private ContainerDeserializer deserializer;

  ContainerSerializationStyle(ContainerDeserializer deserializer) {
    this.deserializer = deserializer;
  }

  public ContainerDeserializer getDeserializer() {
    return deserializer;
  }

}
