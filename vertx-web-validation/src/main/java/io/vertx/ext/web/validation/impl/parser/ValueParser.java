package io.vertx.ext.web.validation.impl.parser;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.validation.MalformedValueException;

public interface ValueParser<T> {

  @Nullable Object parse(T serialized) throws MalformedValueException;

  ValueParser<String> NOOP_PARSER = v -> v;
  ValueParser<String> LONG_PARSER = l -> {
    try {
      return Long.parseLong(l);
    } catch (NumberFormatException e) {
      throw new MalformedValueException(e);
    }
  };
  ValueParser<String> DOUBLE_PARSER = d -> {
    try {
      return Double.parseDouble(d);
    } catch (NumberFormatException e) {
      throw new MalformedValueException(e);
    }
  };
  ValueParser<String> BOOLEAN_PARSER = s -> {
    if (s.equalsIgnoreCase("true")) return true;
    if (s.equalsIgnoreCase("false")) return false;
    throw new MalformedValueException("Value " + s + " should be true or false");
  };
  ValueParser<String> JSON_PARSER = j -> {
    try {
      return Json.decodeValue(j);
    } catch (DecodeException e) {
      throw new MalformedValueException(e);
    }
  };

}
