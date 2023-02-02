/*
 * Copyright 2021 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.impl;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.preparsed.persisted.PersistedQuerySupport;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import org.dataloader.DataLoaderRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.vertx.core.http.HttpMethod.*;

/**
 * @author Thomas Segismont
 */
public class GraphQLHandlerImpl implements GraphQLHandler {
  private static final Pattern IS_NUMBER = Pattern.compile("\\d+");

  private static final Function<RoutingContext, Object> DEFAULT_QUERY_CONTEXT_FACTORY = rc -> rc;
  private static final Function<RoutingContext, DataLoaderRegistry> DEFAULT_DATA_LOADER_REGISTRY_FACTORY = rc -> null;
  private static final Function<RoutingContext, Locale> DEFAULT_LOCALE_FACTORY = rc -> null;

  private final GraphQL graphQL;
  private final GraphQLHandlerOptions options;

  private Function<RoutingContext, Object> queryContextFactory = DEFAULT_QUERY_CONTEXT_FACTORY;
  private Function<RoutingContext, DataLoaderRegistry> dataLoaderRegistryFactory = DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
  private Function<RoutingContext, Locale> localeFactory = DEFAULT_LOCALE_FACTORY;
  private Handler<ExecutionInputBuilderWithContext<RoutingContext>> beforeExecute;

  public GraphQLHandlerImpl(GraphQL graphQL, GraphQLHandlerOptions options) {
    Objects.requireNonNull(graphQL, "graphQL");
    Objects.requireNonNull(options, "options");
    this.graphQL = graphQL;
    this.options = options;
  }

  @Override
  public synchronized GraphQLHandler queryContext(Function<RoutingContext, Object> factory) {
    queryContextFactory = factory != null ? factory : DEFAULT_QUERY_CONTEXT_FACTORY;
    return this;
  }

  @Override
  public synchronized GraphQLHandler dataLoaderRegistry(Function<RoutingContext, DataLoaderRegistry> factory) {
    dataLoaderRegistryFactory = factory != null ? factory : DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
    return this;
  }

  @Override
  public synchronized GraphQLHandler locale(Function<RoutingContext, Locale> factory) {
    localeFactory = factory != null ? factory : DEFAULT_LOCALE_FACTORY;
    return this;
  }

  @Override
  public synchronized GraphQLHandler beforeExecute(Handler<ExecutionInputBuilderWithContext<RoutingContext>> beforeExecute) {
    this.beforeExecute = beforeExecute;
    return this;
  }

  @Override
  public void handle(RoutingContext rc) {
    HttpMethod method = rc.request().method();
    if (method == GET) {
      handleGet(rc);
    } else if (method == POST) {
      if (!rc.body().available()) {
        // the body handler was not set, so we cannot securely process POST bodies
        // we could just add an ad-hoc body handler but this can lead to DDoS attacks
        // and it doesn't really cover all the uploads, such as multipart, etc...
        // as well as resource cleanup
        rc.fail(500, new NoStackTraceThrowable("BodyHandler is required to process POST requests"));
      } else {
        handlePost(rc, rc.body().buffer());
      }
    } else {
      rc.fail(405);
    }
  }

  private void handleGet(RoutingContext rc) {
    Map<String, Object> variables;
    try {
      variables = getVariablesFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }
    Object initialValue;
    try {
      initialValue = getInitialValueFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }
    Map<String, Object> extensions;
    try {
      extensions = getExtensionsFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }
    String query = rc.queryParams().get("query");
    if (query == null) {
      if (extensions != null && extensions.containsKey("persistedQuery")) {
        query = PersistedQuerySupport.PERSISTED_QUERY_MARKER;
      } else {
        failQueryMissing(rc);
        return;
      }
    }
    GraphQLQuery graphQLQuery = new GraphQLQuery()
      .setQuery(query)
      .setOperationName(rc.queryParams().get("operationName"))
      .setVariables(variables)
      .setInitialValue(initialValue)
      .setExtensions(extensions);
    executeOne(rc, graphQLQuery);
  }

  private void handlePost(RoutingContext rc, Buffer body) {
    Map<String, Object> variables;
    try {
      variables = getVariablesFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }

    Object initialValue;
    try {
      initialValue = getInitialValueFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }

    Map<String, Object> extensions;
    try {
      extensions = getExtensionsFromQueryParam(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }

    String query = rc.queryParams().get("query");
    if (query != null) {
      GraphQLQuery graphQLQuery = new GraphQLQuery()
        .setQuery(query)
        .setOperationName(rc.queryParams().get("operationName"))
        .setVariables(variables)
        .setInitialValue(initialValue)
        .setExtensions(extensions);
      executeOne(rc, graphQLQuery);
      return;
    }

    switch (getContentType(rc)) {
      case "application/json":
        if (body == null) {
          // plain failure as the json body is missing
          rc.fail(400, new NoStackTraceThrowable("No body"));
          return;
        }
        handlePostJson(rc, body, rc.queryParams().get("operationName"), variables, initialValue, extensions);
        break;
      case "multipart/form-data":
        handlePostMultipart(rc, rc.queryParams().get("operationName"), variables, initialValue, extensions);
        break;
      case "application/graphql":
        if (body == null) {
          // plain failure as the query is missing
          rc.fail(400, new NoStackTraceThrowable("No body"));
          return;
        }
        GraphQLQuery graphQLQuery = new GraphQLQuery()
          .setQuery(body.toString())
          .setOperationName(rc.queryParams().get("operationName"))
          .setVariables(variables)
          .setInitialValue(initialValue)
          .setExtensions(extensions);
        executeOne(rc, graphQLQuery);
        break;
      default:
        rc.fail(415);
    }
  }

  private void handlePostJson(RoutingContext rc, Buffer body, String operationName, Map<String, Object> variables, Object initialValue, Map<String, Object> extensions) {
    GraphQLInput graphQLInput;
    try {
      graphQLInput = GraphQLInput.decode(body);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }
    if (graphQLInput instanceof GraphQLBatch) {
      handlePostBatch(rc, (GraphQLBatch) graphQLInput, operationName, variables, initialValue, extensions);
    } else if (graphQLInput instanceof GraphQLQuery) {
      handlePostQuery(rc, (GraphQLQuery) graphQLInput, operationName, variables, initialValue, extensions);
    } else {
      rc.fail(500);
    }
  }

  private void handlePostBatch(RoutingContext rc, GraphQLBatch batch, String operationName, Map<String, Object> variables, Object initialValue, Map<String, Object> extensions) {
    if (!options.isRequestBatchingEnabled()) {
      rc.fail(400);
      return;
    }
    for (GraphQLQuery query : batch) {
      if (operationName != null) {
        query.setOperationName(operationName);
      }
      if (variables != null) {
        query.setVariables(variables);
      }
      if (initialValue != null) {
        query.setInitialValue(initialValue);
      }
      if (extensions != null) {
        query.setExtensions(extensions);
      }
      if (query.getQuery() == null) {
        Map<String, Object> exts = query.getExtensions();
        if (exts != null && exts.containsKey("persistedQuery")) {
          query.setQuery(PersistedQuerySupport.PERSISTED_QUERY_MARKER);
        } else {
          failQueryMissing(rc);
          return;
        }
      }
    }
    executeBatch(rc, batch);
  }

  @SuppressWarnings("rawtypes")
  private void executeBatch(RoutingContext rc, GraphQLBatch batch) {
    List<Future> futures = new ArrayList<>(batch.size());
    for (GraphQLQuery graphQLQuery : batch) {
      futures.add(execute(rc, graphQLQuery));
    }
    CompositeFuture.all(futures)
      .map(cf -> new JsonArray(cf.list()).toBuffer())
      .onComplete(ar -> sendResponse(rc, ar));
  }

  private void handlePostQuery(RoutingContext rc, GraphQLQuery query, String operationName, Map<String, Object> variables, Object initialValue, Map<String, Object> extensions) {
    if (operationName != null) {
      query.setOperationName(operationName);
    }
    if (variables != null) {
      query.setVariables(variables);
    }
    if (initialValue != null) {
      query.setInitialValue(initialValue);
    }
    if (extensions != null) {
      query.setExtensions(extensions);
    }
    if (query.getQuery() == null) {
      Map<String, Object> exts = query.getExtensions();
      if (exts != null && exts.containsKey("persistedQuery")) {
        query.setQuery(PersistedQuerySupport.PERSISTED_QUERY_MARKER);
      } else {
        failQueryMissing(rc);
        return;
      }
    }
    executeOne(rc, query);
  }

  /**
   * An "operations object" is an Apollo GraphQL POST request (or array of requests if batching).
   * An "operations path" is an object-path string to locate a file within an operations object.
   * <p>
   * So operations can be resolved while the files are still uploading, the fields are ordered:
   * <p>
   * 1. operations: A JSON encoded operations object with files replaced with null.
   * 2. map: A JSON encoded map of where files occurred in the operations. For each file, the key is
   * the file multipart form field name and the value is an array of operations paths.
   * 3. File fields: Each file extracted from the operations object with a unique, arbitrary field name.
   *
   * @see <a href="https://github.com/jaydenseric/graphql-multipart-request-spec">GraphQL multipart request specification</a>
   **/
  private void handlePostMultipart(RoutingContext rc, String operationName, Map<String, Object> variables, Object initialValue, Map<String, Object> extensions) {
    GraphQLInput graphQLInput;
    if (!options.isRequestMultipartEnabled()) {
      rc.fail(415);
      return;
    }

    try {
      graphQLInput = parseMultipartAttributes(rc);
    } catch (Exception e) {
      rc.fail(400, e);
      return;
    }

    if (graphQLInput instanceof GraphQLBatch) {
      handlePostBatch(rc, (GraphQLBatch) graphQLInput, operationName, variables, initialValue, extensions);
    } else if (graphQLInput instanceof GraphQLQuery) {
      handlePostQuery(rc, (GraphQLQuery) graphQLInput, operationName, variables, initialValue, extensions);
    } else {
      rc.fail(500);
    }
  }

  private GraphQLInput parseMultipartAttributes(RoutingContext rc) {
    MultiMap attrs = rc.request().formAttributes();
    @SuppressWarnings("unchecked")
    Map<String, Object> filesMap = (Map<String, Object>) Json.decodeValue(attrs.get("map"), Map.class);

    GraphQLInput graphQLInput = GraphQLInput.decode(Json.decodeValue(attrs.get("operations")));
    Map<String, Map<String, Object>> variablesMap = new HashMap<>();

    Iterable<GraphQLQuery> batch = (graphQLInput instanceof GraphQLBatch)
      ? (GraphQLBatch) graphQLInput
      : Collections.singletonList((GraphQLQuery) graphQLInput);

    int i = 0;
    Iterator<GraphQLQuery> iterator = batch.iterator();
    for (; iterator.hasNext(); i++) {
      GraphQLQuery query = iterator.next();
      Map<String, Object> variables = new HashMap<>();
      variables.put("variables", query.getVariables());
      variablesMap.put(String.valueOf(i), variables);
    }

    for (Map.Entry<String, Object> entry : filesMap.entrySet()) {
      for (Object fullPath : (List) entry.getValue()) {
        String[] path = ((String) fullPath).split("\\.");
        int end = path.length;

        int idx = -1;

        if (IS_NUMBER.matcher(path[end - 1]).matches()) {
          idx = Integer.parseInt(path[end - 1]);
          --end;
        }

        Map<?, ?> variables;

        int start = 0;
        if (IS_NUMBER.matcher(path[0]).matches()) {
          variables = variablesMap.get(path[0]);
          ++start;
        } else {
          variables = variablesMap.get("0");
        }

        String attr = path[--end];
        Map obj = variables;
        for (; start < end; ++start) {
          String token = path[start];
          obj = (Map) obj.get(token);
        }

        FileUpload file = rc.fileUploads().stream()
          .filter(f -> f.name().equals(entry.getKey())).findFirst().orElse(null);

        if (file != null) {
          if (idx == -1) {
            obj.put(attr, file);
          } else {
            ((List) obj.get(attr)).set(idx, file);
          }
        }
      }
    }

    return graphQLInput;
  }

  private void executeOne(RoutingContext rc, GraphQLQuery query) {
    execute(rc, query)
      .map(JsonObject::toBuffer)
      .onComplete(ar -> sendResponse(rc, ar));
  }

  private Future<JsonObject> execute(RoutingContext rc, GraphQLQuery query) {
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();

    builder.query(query.getQuery());
    String operationName = query.getOperationName();
    if (operationName != null) {
      builder.operationName(operationName);
    }
    Map<String, Object> variables = query.getVariables();
    if (variables != null) {
      builder.variables(variables);
    }
    Object initialValue = query.getInitialValue();
    if (initialValue != null) {
      builder.root(initialValue);
    }
    Map<String, Object> extensions = query.getExtensions();
    if (extensions != null) {
      builder.extensions(extensions);
    }

    Function<RoutingContext, Object> qc;
    Function<RoutingContext, DataLoaderRegistry> dlr;
    Function<RoutingContext, Locale> l;
    Handler<ExecutionInputBuilderWithContext<RoutingContext>> be;
    synchronized (this) {
      qc = queryContextFactory;
      dlr = dataLoaderRegistryFactory;
      l = localeFactory;
      be = beforeExecute;
    }

    builder.context(qc.apply(rc));

    DataLoaderRegistry registry = dlr.apply(rc);
    if (registry != null) {
      builder.dataLoaderRegistry(registry);
    }

    Locale locale = l.apply(rc);
    if (locale != null) {
      builder.locale(locale);
    }

    builder.graphQLContext(Collections.singletonMap(RoutingContext.class, rc));

    if (be != null) {
      be.handle(new ExecutionInputBuilderWithContext<RoutingContext>() {
        @Override
        public RoutingContext context() {
          return rc;
        }

        @Override
        public ExecutionInput.Builder builder() {
          return builder;
        }
      });
    }

    return Future.fromCompletionStage(graphQL.executeAsync(builder.build()), rc.vertx().getOrCreateContext())
      .map(executionResult -> new JsonObject(executionResult.toSpecification()));
  }

  private String getContentType(RoutingContext rc) {
    String contentType = rc.parsedHeaders().contentType().value();
    return contentType.isEmpty() ? "application/json" : contentType.toLowerCase();
  }

  private Map<String, Object> getVariablesFromQueryParam(RoutingContext rc) throws Exception {
    String variablesParam = rc.queryParams().get("variables");
    if (variablesParam == null) {
      return null;
    } else {
      return new JsonObject(variablesParam).getMap();
    }
  }

  private Object getInitialValueFromQueryParam(RoutingContext rc) throws Exception {
    String initialParam = rc.queryParams().get("initialValue");
    if (initialParam == null || initialParam.isEmpty()) {
      return null;
    } else {
      return Json.decodeValue(initialParam);
    }
  }

  private Map<String, Object> getExtensionsFromQueryParam(RoutingContext rc) throws Exception {
    String extensionsParam = rc.queryParams().get("extensions");
    if (extensionsParam == null) {
      return null;
    } else {
      return new JsonObject(extensionsParam).getMap();
    }
  }

  private void sendResponse(RoutingContext rc, AsyncResult<Buffer> ar) {
    if (ar.succeeded()) {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(ar.result());
    } else {
      rc.fail(ar.cause());
    }
  }

  private void failQueryMissing(RoutingContext rc) {
    rc.fail(400, new NoStackTraceThrowable("Query is missing"));
  }
}
