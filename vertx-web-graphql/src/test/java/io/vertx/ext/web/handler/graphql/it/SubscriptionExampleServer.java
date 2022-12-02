package io.vertx.ext.web.handler.graphql.it;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import io.reactivex.Flowable;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.Link;
import io.vertx.ext.web.handler.graphql.User;
import io.vertx.reactivex.RxHelper;
import org.reactivestreams.Publisher;

public class SubscriptionExampleServer extends GraphQLServer {
  private final List<Link> links;

  public SubscriptionExampleServer() {
    final User peter = new User("1", "Peter");
    final User paul = new User("2", "Paul");
    final User jack = new User("3", "Jack");

    links = new ArrayList<>();
    links.add(new Link("https://vertx.io", "Vert.x project", peter.getName()));
    links.add(new Link("https://www.eclipse.org", "Eclipse Foundation", paul.getName()));
    links.add(new Link("http://reactivex.io", "ReactiveX libraries", jack.getName()));
    links.add(new Link("https://www.graphql-java.com", "GraphQL Java implementation", peter.getName()));
  }

  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(getVertx());
    router.route("/graphql").handler(ApolloWSHandler.create(createGraphQL()));
    router.get("/health").handler(routingContext -> routingContext.response().end("OK"));
    HttpServerOptions httpServerOptions = new HttpServerOptions().addWebSocketSubProtocol("graphql-ws");
    vertx.createHttpServer(httpServerOptions).requestHandler(router)
      .listen(8080, httpServerAsyncResult -> {
        if (httpServerAsyncResult.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(httpServerAsyncResult.cause());
        }
      });
  }

  @Override
  public String getSchema() {
    return vertx.fileSystem().readFileBlocking("links.graphqls").toString();
  }

  @Override
  public RuntimeWiring getWiring() {
    return RuntimeWiring.newRuntimeWiring()
      .type("Subscription", builder -> builder.dataFetcher("links", this::linksFetcher))
      .build();
  }

  private Publisher<Link> linksFetcher(DataFetchingEnvironment dataFetchingEnvironment) {
    return Flowable.interval(1, TimeUnit.SECONDS) //Ticks
      .zipWith(Flowable.fromIterable(links), (tick, link) -> link) //Emit link on each tick
      .observeOn(RxHelper.scheduler(context));
  }
}
