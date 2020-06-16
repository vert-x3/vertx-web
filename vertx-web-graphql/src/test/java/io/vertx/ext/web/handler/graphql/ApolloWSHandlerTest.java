/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.ext.web.handler.graphql.ApolloWSMessageType.COMPLETE;
import static io.vertx.ext.web.handler.graphql.ApolloWSMessageType.DATA;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerTest extends WebTestBase {

  private static final int MAX_COUNT = 4;
  private static final int STATIC_COUNT = 5;

  private ApolloWSOptions apolloWSOptions = new ApolloWSOptions();
  private AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    GraphQL graphQL = graphQL();
    router.route("/graphql").handler(ApolloWSHandler.create(graphQL, apolloWSOptions));
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
  }

  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("counter.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("staticCounter", this::getStaticCounter))
      .type("Subscription", builder -> builder.dataFetcher("counter", this::getCounter))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private Map<String, Object> getStaticCounter(DataFetchingEnvironment env) {
    int count = env.getArgument("num");
    Map<String, Object> counter = new HashMap<>();
    counter.put("count", count);
    return counter;
  }

  private Publisher<Map<String, Object>> getCounter(DataFetchingEnvironment env) {
    boolean finite = env.getArgument("finite");
    return subscriber -> {
      Subscription subscription = new Subscription() {
        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
          if (!subscriptionRef.compareAndSet(this, null)) {
            fail();
          }
        }
      };
      if (!subscriptionRef.compareAndSet(null, subscription)) {
        fail();
      }
      subscriber.onSubscribe(subscription);
      IntStream.range(0, 5).forEach(num -> {
        Map<String, Object> counter = new HashMap<>();
        counter.put("count", num);
        subscriber.onNext(counter);
      });
      if (finite) {
        subscriber.onComplete();
        if (!subscriptionRef.compareAndSet(subscription, null)) {
          fail();
        }
      }
    };
  }

  @Test
  public void testSubscriptionWsCall() {
    waitFor(MAX_COUNT + 2);
    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicReference<String> id = new AtomicReference<>();
      AtomicInteger counter = new AtomicInteger();
      websocket.textMessageHandler(text -> {
        JsonObject obj = new JsonObject(text);
        int current = counter.getAndIncrement();
        if (current >= 0 && current <= MAX_COUNT) {
          if (current == 0) {
            assertTrue(id.compareAndSet(null, obj.getString("id")));
          } else {
            assertEquals(id.get(), obj.getString("id"));
          }
          assertEquals(DATA, ApolloWSMessageType.from(obj.getString("type")));
          int val = obj.getJsonObject("payload").getJsonObject("data").getJsonObject("counter").getInteger("count");
          assertEquals(current, val);
          complete();
        } else if (current == MAX_COUNT + 1) {
          assertEquals(id.get(), obj.getString("id"));
          assertEquals(COMPLETE, ApolloWSMessageType.from(obj.getString("type")));
          complete();
        } else {
          fail();
        }
      });

      JsonObject message = new JsonObject()
        .put("payload", new JsonObject()
          .put("query", "subscription Subscription { counter { count } }"))
        .put("type", "start")
        .put("id", "1");
      websocket.write(message.toBuffer());
    }));
    await();
  }

  @Test
  public void testQueryWsCall() {
    testQueryWsCall((webSocket, message) -> webSocket.write(message.toBuffer()));
  }

  @Test
  public void testQueryWsCallMultipleFrames() {
    testQueryWsCall((webSocket, message) -> {
      Buffer buffer = message.toBuffer();
      int part = buffer.length() / 3;
      if (part == 0) fail("Cannot perform test");
      webSocket.writeFrame(WebSocketFrame.binaryFrame(buffer.getBuffer(0, part), false));
      webSocket.writeFrame(WebSocketFrame.continuationFrame(buffer.getBuffer(part, 2 * part), false));
      webSocket.writeFrame(WebSocketFrame.continuationFrame(buffer.getBuffer(2 * part, buffer.length()), true));
    });
  }

  private void testQueryWsCall(BiConsumer<WebSocket, JsonObject> sender) {
    waitFor(2);
    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicReference<String> id = new AtomicReference<>();
      AtomicInteger counter = new AtomicInteger();
      websocket.textMessageHandler(text -> {
        JsonObject obj = new JsonObject(text);
        int current = counter.getAndIncrement();
        if (current == 0) {
          assertTrue(id.compareAndSet(null, obj.getString("id")));
          assertEquals(DATA, ApolloWSMessageType.from(obj.getString("type")));
          int val = obj.getJsonObject("payload").getJsonObject("data").getJsonObject("staticCounter").getInteger("count");
          assertEquals(STATIC_COUNT, val);
          complete();
        } else if (current == 1) {
          assertEquals(id.get(), obj.getString("id"));
          assertEquals(COMPLETE, ApolloWSMessageType.from(obj.getString("type")));
          complete();
        } else {
          fail();
        }
      });

      JsonObject message = new JsonObject()
        .put("payload", new JsonObject()
          .put("query", "query Query { staticCounter { count } }"))
        .put("type", "start")
        .put("id", "1");
      sender.accept(websocket, message);
    }));
    await();
  }

  @Test
  public void testQueryHttpCall() throws Exception {
    String query = "query Query { staticCounter { count } }";
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery(query);
    request.send(client, onSuccess(body -> {
      int count = body.getJsonObject("data")
        .getJsonObject("staticCounter")
        .getInteger("count");
      assertEquals(STATIC_COUNT, count);
      complete();
    }));
    await();
  }

  @Test
  public void testWsKeepAlive() {
    apolloWSOptions.setKeepAlive(100L);

    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicInteger counter = new AtomicInteger(0);
      websocket.textMessageHandler(text -> {
        try {
          JsonObject obj = new JsonObject(text);

          if (counter.getAndIncrement() == 0) {
            assertEquals(ApolloWSMessageType.CONNECTION_ACK.getText(), obj.getString("type"));
          } else {
            assertEquals(ApolloWSMessageType.CONNECTION_KEEP_ALIVE.getText(), obj.getString("type"));
            complete();
          }
        } catch (Exception e) {
          fail(e);
        }
      });

      JsonObject message = new JsonObject()
        .put("type", "connection_init");
      websocket.write(message.toBuffer());
    }));
    await();
  }

  @Test
  public void testSubscriptionCanceledOnAbruptClose() throws Exception {
    HttpClientOptions clientOptions = getHttpClientOptions();
    int backendPort = clientOptions.getDefaultPort();
    int proxyPort = backendPort + 101;

    Proxy proxy = new Proxy(clientOptions.getDefaultHost(), proxyPort, backendPort);
    proxy.start();
    client.close();
    client = vertx.createHttpClient(clientOptions.setDefaultPort(proxyPort));

    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicInteger counter = new AtomicInteger();
      websocket.textMessageHandler(text -> {
        if (counter.getAndIncrement() == MAX_COUNT) {
          if (subscriptionRef.get() == null) {
            fail("Expected a live subscription");
          } else {
            proxy.closeAbruptly(onSuccess(v -> {
              testComplete();
            }));
          }
        }
      });

      JsonObject message = new JsonObject()
        .put("payload", new JsonObject()
          .put("query", "subscription Subscription { counter(finite: false) { count } }"))
        .put("type", "start")
        .put("id", "1");
      websocket.write(message.toBuffer());
    }));
    await();

    assertWaitUntil(() -> subscriptionRef.get() == null);
  }

  // We need this proxy to make sure the connection to the backend is reset abruptly
  // Otherwise the Vert.x HttpClient closes the websocket properly before closing the TCP connection
  private class Proxy {
    final String host;
    final int serverPort, clientPort;

    volatile NetServer server;
    volatile NetSocket client;

    Proxy(String host, int serverPort, int clientPort) {
      this.host = host;
      this.serverPort = serverPort;
      this.clientPort = clientPort;
    }

    void start() throws Exception {
      CountDownLatch latch = new CountDownLatch(1);
      vertx.createNetServer()
        .exceptionHandler(Throwable::printStackTrace)
        .connectHandler(socket -> {
          socket.pause();
          vertx.createNetClient(new NetClientOptions().setSoLinger(0))
            .connect(clientPort, host, onSuccess(client -> {
              this.client = client;
              socket.pipeTo(client, v -> socket.close());
              client.pipeTo(socket, v -> socket.close());
              socket.resume();
            }));
        })
        .listen(serverPort, host, onSuccess(server -> {
          this.server = server;
          latch.countDown();
        }));
      awaitLatch(latch);
    }

    void closeAbruptly(Handler<AsyncResult<Void>> handler) {
      client.close(ar -> {
        server.close();
        handler.handle(ar);
      });
    }
  }
}
