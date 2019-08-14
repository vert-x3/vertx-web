package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.api.validation.impl.SplitterCharContainerDeserializer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This enum contains supported object and arrays serialization styles. Every style has a enum value, and an array of
 * strings to refeer to it.
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ContainerSerializationStyle {

  /**
   * Comma separated values: "value1,value2,value3"
   * aliases: "csv", "commaDelimited", "form", "simple"
   */
  csv(new String[]{"csv", "commaDelimited", "form", "simple", "matrix"}, new SplitterCharContainerDeserializer(Pattern.quote(","))),

  /**
   * Space separated values: "value1 value2 value3"
   * aliases: "ssv", "spaceDelimited"
   */
  ssv(new String[]{"ssv", "spaceDelimited"}, new SplitterCharContainerDeserializer("\\s+")),

  /**
   * Pipe separated values: "value1|value2|value3"
   * aliases: "psv", "pipeDelimited"
   */
  psv(new String[]{"psv", "pipeDelimited"}, new SplitterCharContainerDeserializer(Pattern.quote("|"))),

  /**
   * Dot delimited values: "value1.value2.value3"
   * aliases: "dsv", "dotDelimited", "label"
   */
  dsv(new String[]{"dsv", "dotDelimited", "label"}, new SplitterCharContainerDeserializer(Pattern.quote("."))),

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
      String[] values = serialized.split(Pattern.quote(","), -1);
      // Key value pairs -> odd length not allowed
      for (String value : values) {
        // empty key not allowed!
        String[] values_internal = value.split("=", -1);
        if (values_internal[0].length() == 0) {
          throw ValidationException.ValidationExceptionFactory.generateDeserializationError("DeserializationError: " +
            "" + "Empty key not allowed");
        } else {
          result.put(values_internal[0], values_internal[1]);
        }
      }
      return result;
    }
  }),

  /**
   * For internal usage, don't use it
   */
  matrix_exploded_array(new String[]{"matrix_exploded_array"}, new ContainerDeserializer() {

    private final Pattern MATRIX_PARAMETER = Pattern.compile(";(?<key>[^;=]*)=(?<value>[^\\/\\;\\?\\:\\@\\&\\\"\\<\\>\\#\\%\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)");

    @Override
    public List<String> deserializeArray(String serialized) throws ValidationException {
      List<String> values = new ArrayList<>();
      Matcher m = MATRIX_PARAMETER.matcher(serialized);
      while (m.find())
        values.add(URIDecoder.decodeURIComponent(m.group("value"), false));
      return values;
    }

    @Override
    public Map<String, String> deserializeObject(String serialized) throws ValidationException {
      return null;
    }
  });

  private ContainerDeserializer deserializer;
  private List<String> names;

  ContainerSerializationStyle(String[] names, ContainerDeserializer deserializer) {
    this.names = Arrays.asList(names);
    this.deserializer = deserializer;
  }

  public ContainerDeserializer deserializer() {
    return deserializer;
  }

  public List<String> names() {
    return names;
  }

  public static ContainerSerializationStyle getContainerStyle(String s) {
    for (ContainerSerializationStyle style : ContainerSerializationStyle.values()) {
      if (style.names().contains(s)) return style;
    }
    return ContainerSerializationStyle.csv;
  }

}
