package io.vertx.ext.web.handler.graphql.it;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class HelloGraphQLServer extends GraphQLServer {

  private static final Counter counter = new Counter();

  private final Person[] philosophers;

  public HelloGraphQLServer() {
    final Person plato = new Person("Plato");
    final Person aristotle = new Person("Aristotle");
    plato.setFriend(aristotle);
    aristotle.setFriend(plato);
    philosophers = new Person[]{plato, aristotle};
  }

  @Override
  public String getSchema() {
    return getVertx().fileSystem().readFileBlocking("types.graphqls").toString();
  }

  @Override
  public RuntimeWiring getWiring() {
    final GraphQLScalarType datetime = GraphQLScalarType.newScalar()
      .name("Datetime")
      .coercing(new DatetimeCoercion())
      .build();
    return newRuntimeWiring()
      .scalar(datetime)
      .type("Query", builder -> {
        final HashMap<String, DataFetcher> fetchersMap = new HashMap<>();
        fetchersMap.put("floating", env -> 3.14f);
        fetchersMap.put("bool", env -> true);
        fetchersMap.put("id", env -> "1001");
        fetchersMap.put("enum", env -> Musketeer.ATHOS);
        fetchersMap.put("when", env -> LocalDateTime.of(1991, 8, 25, 22, 57, 8));
        fetchersMap.put("answer", env -> "Hello, " + env.getArgument("name") + "!");
        fetchersMap.put("array", env -> new String[]{"apples", "eggs", "carrots"});
        fetchersMap.put("list", env -> Arrays.asList("apples", "eggs", "carrots"));
        return builder.dataFetcher("hello", env -> "Hello World!")
          .dataFetcher("number", env -> 130)
          .dataFetcher("changing", counter)
          .dataFetcher("persons", env -> philosophers)
          .dataFetchers(fetchersMap);
      })
      .build();
  }
}

class Counter implements DataFetcher<Integer> {
  private final AtomicInteger order = new AtomicInteger(0);

  @Override
  public Integer get(DataFetchingEnvironment environment) {
    return order.getAndIncrement();
  }
}
