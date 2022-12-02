package io.vertx.ext.web.handler.graphql.it;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.Link;
import io.vertx.ext.web.handler.graphql.User;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class ExampleServer extends GraphQLServer {

  private final List<Link> links;

  public ExampleServer() {
    final User a = new User("00001", "Anthony");
    final User b = new User("00002", "Barny");
    final User c = new User("00003", "Cooper");

    links = new ArrayList<>();
    links.add(new Link("https://vertx.io", "Vert.x project", a.getId()));
    links.add(new Link("https://www.eclipse.org", "Eclipse Foundation", b.getId()));
    links.add(new Link("http://reactivex.io", "ReactiveX libraries", c.getId()));
    links.add(new Link("https://www.graphql-java.com", "GraphQL Java implementation", a.getId()));
  }

  @Override
  public String getSchema() {
    return vertx.fileSystem().readFileBlocking("links.graphqls").toString();
  }

  @Override
  public RuntimeWiring getWiring() {
    return newRuntimeWiring()
      .type("Query", builder -> {
        VertxDataFetcher<List<Link>> getAllLinks = VertxDataFetcher.create(this::getAllLinks);
        return builder.dataFetcher("allLinks", getAllLinks);
      })
      .build();
  }

  private void getAllLinks(DataFetchingEnvironment environment, Promise<List<Link>> promise) {

  }
}
