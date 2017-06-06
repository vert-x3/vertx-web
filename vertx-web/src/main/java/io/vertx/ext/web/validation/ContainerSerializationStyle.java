package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.SplitterCharContainerDeserializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This enum contains supported object and arrays serialization styles. Every style has a enum value, and an array of strings to refeer to it.
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ContainerSerializationStyle {

  /**
   * Comma separated values: "value1,value2,value3"
   * aliases: "csv", "commaDelimited", "form", "simple"
   */
  csv(new String[]{"csv", "commaDelimited", "form", "simple"}, new SplitterCharContainerDeserializer(",")),
  /**
   * Space separated values: "value1 value2 value3"
   * aliases: "ssv", "spaceDelimited"
   */
  ssv(new String[]{"ssv", "spaceDelimited"}, new SplitterCharContainerDeserializer("\\s+")),
  /**
   * Pipe separated values: "value1|value2|value3"
   * aliases: "psv", "pipeDelimited"
   */
  psv(new String[]{"psv", "pipeDelimited"}, new SplitterCharContainerDeserializer("|")),
  /**
   * For internal usage, don't use it
   */
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
          throw ValidationException.ValidationExceptionFactory.generateDeserializationError("DeserializationError: Empty key not allowed");
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
