package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.SplitterCharContainerDeserializer;

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

  csv(new String[]{"csv", "commaDelimited", "form", "simple"}, new SplitterCharContainerDeserializer(",")),
  ssv(new String[]{"ssv", "spaceDelimited"}, new SplitterCharContainerDeserializer("\\s+")),
  psv(new String[]{"psv", "pipeDelimited"}, new SplitterCharContainerDeserializer("|")),
  simple_exploded_object(new String[]{"simple_exploded_object"}, new ContainerDeserializer() {

    @Override
    public List<String> deserializeArray(String serialized) throws ValidationException {
      return null;
    }

    @Override
    public Map<String, String> deserializeObject(String serialized) throws ValidationException {
      Map<String, String> result = new HashMap<>();
      String[] values = serialized.split(",", -1);
      // Key value pairs -> odd length not allowed
      for (int i = 0; i < values.length; i++) {
        // empty key not allowed!
        String[] values_internal = values[i].split("=", -1);
        if (values_internal[0].length() == 0) {
          throw ValidationException.generateDeserializationError("DeserializationError: Empty key not allowed");
        } else {
          result.put(values_internal[0], values_internal[1]);
        }
      }
      return result;
    }
  });


  private ContainerDeserializer deserializer;
  private List<String> names;

  ContainerSerializationStyle(String[] names, ContainerDeserializer deserializer) {
    this.names = Arrays.asList(names);
    this.deserializer = deserializer;
  }

  public ContainerDeserializer getDeserializer() {
    return deserializer;
  }

  public List<String> getNames() {
    return names;
  }

  public static ContainerSerializationStyle getContainerStyle(String s) {
    for (ContainerSerializationStyle style : ContainerSerializationStyle.values()) {
      if (style.getNames().contains(s))
        return style;
    }
    return ContainerSerializationStyle.csv;
  }

}
