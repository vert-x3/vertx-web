package io.vertx.ext.web.handler.graphql;

import java.math.BigInteger;
import java.util.Arrays;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import io.vertx.ext.web.handler.graphql.it.GraphQLServer;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class BorderServer extends GraphQLServer {

  private final static String VERY_LONG = generateLongString();

  private static String generateLongString() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < 110_116; i++) {
      stringBuilder.append((char) i);
    }
    return stringBuilder.toString();
  }

  @Override
  public String getSchema() {
    return vertx.fileSystem().readFileBlocking("borders.graphqls").toString();
  }

  @Override
  public RuntimeWiring getWiring() {
    return newRuntimeWiring()
      .type("Query",
            builder -> builder
              .dataFetcher("text", new TextFetcher())
              .dataFetcher("number", new IntegerFetcher())
              .dataFetcher("floating", new FloatFetcher())
              .dataFetcher("bool", new BoolFetcher())
              .dataFetcher("list", new ListFetcher())
              .dataFetcher("array", new ListFetcher())
      )
      .build();
  }

  private String getType(DataFetchingEnvironment environment) {
    return environment.getArgument("type");
  }

  class InvalidTypeException extends IllegalArgumentException {
    public InvalidTypeException(DataFetchingEnvironment env) {
      super("Unexpected value: " + getType(env));
    }
  }

  private class TextFetcher implements DataFetcher<String> {

    @Override
    public String get(DataFetchingEnvironment environment) throws Exception {
      switch (getType(environment)) {
        case "valid":
          return "hello";
        case "null":
          return null;
        case "eol":
          return "a\nb\r\nc\0d e\tf";
        case "non-ascii":
          return "今日は přítel, как дела?";
        case "empty":
          return "";
        case "brokenjson":
          return "}";
        case "long":
          return VERY_LONG;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class IntegerFetcher implements DataFetcher {
    @Override
    public Object get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "positive":
          return 10;
        case "negative":
          return -10;
        case "max":
          return Integer.MAX_VALUE;
        case "min":
          return Integer.MIN_VALUE;
        case "huge":
          return 1L + Integer.MAX_VALUE;
        case "tiny":
          return -1L + Integer.MIN_VALUE;
        case "zero":
          return 0;
        case "overwhelming":
          return BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        case "float":
          return 3.14f;
        case "string":
          return "hi";
        case "null":
          return null;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class FloatFetcher implements DataFetcher<Float> {
    @Override
    public Float get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "valid":
          return 3.14f;
        case "null":
          return null;
        case "nan":
          return Float.NaN;
        case "infinity":
          return Float.POSITIVE_INFINITY;
        case "infinity_neg":
          return Float.NEGATIVE_INFINITY;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class BoolFetcher implements DataFetcher<Boolean> {
    @Override
    public Boolean get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "yes":
          return true;
        case "null":
          return null;
        case "no":
          return false;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class ListFetcher implements DataFetcher {
    @Override
    public Object get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "valid":
          return new String[]{"one", "two"};
        case "object":
          return Arrays.asList("one", "two");
        case "empty":
          return new String[]{};
        case "null":
          return null;
        case "nullvalues":
          return new String[]{null, null};
        case "scalar":
          return "three";
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }
}
