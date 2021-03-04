package io.vertx.ext.web.openapi.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;
import io.vertx.json.schema.Schema;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.common.SchemaURNId;
import io.vertx.json.schema.common.URIUtils;
import io.vertx.json.schema.draft7.Draft7SchemaParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OpenAPIHolderImpl implements OpenAPIHolder {

  private final Vertx vertx;
  private final Map<URI, JsonObject> absolutePaths;
  private final HttpClient client;
  private final FileSystem fs;
  private final SchemaRouter router;
  private final SchemaParser parser;
  private final Schema openapiSchema;
  private final OpenAPILoaderOptions options;
  private URI initialScope;
  private String initialScopeDirectory;
  private final Map<URI, Future<JsonObject>> externalSolvingRefs;
  private final YAMLMapper yamlMapper;
  private JsonObject openapiRoot;

  public OpenAPIHolderImpl(Vertx vertx, HttpClient client, FileSystem fs, OpenAPILoaderOptions options) {
    this.vertx = vertx;
    absolutePaths = new ConcurrentHashMap<>();
    externalSolvingRefs = new ConcurrentHashMap<>();
    this.client = client;
    this.fs = fs;
    this.options = options;
    this.router = SchemaRouter.create(client, fs, options.toSchemaRouterOptions());
    this.parser = Draft7SchemaParser.create(this.router);
    this.yamlMapper = new YAMLMapper();
    this.openapiSchema = parser.parseFromString(OpenAPI3Utils.openapiSchemaJson);
  }

  public Future<JsonObject> loadOpenAPI(String u) {
    URI uri = URIUtils.removeFragment(URI.create(u));
    return ((URIUtils.isRemoteURI(uri)) ? solveRemoteRef(uri) : solveLocalRef(uri))
      .onSuccess(openapiBytes -> {
        // If we're here, the file exists.
        // Now we need to set the proper initialScope and initialScopeDirectory in order
        // to properly resolve refs
        if (URIUtils.isRemoteURI(uri) || uri.isAbsolute()) {
          initialScope = uri;
        } else {
          initialScope = getResourceAbsoluteURI(uri);
        }
        initialScopeDirectory = resolveContainingDirPath(initialScope);
      })
      .compose(openapi -> {
        absolutePaths.put(initialScope, openapi); // Circular refs hell!
        openapiRoot = openapi;
        return walkAndSolve(openapi, initialScope).map(openapi);
      })
      .compose(openapi -> {
        JsonObject openapiCopy = openapi.copy();
        deepSubstituteForValidation(openapiCopy, JsonPointer.fromURI(initialScope), new HashMap<>());
        // We need this flattened spec just to validate it
        return openapiSchema.validateAsync(openapiCopy).map(openapi);
      });
  }

  @Override
  public JsonObject getCached(JsonPointer pointer) {
    JsonObject startingObj = absolutePaths.get(resolveRefResolutionURIWithoutFragment(pointer.getURIWithoutFragment()
      , initialScope));
    return (JsonObject) pointer.queryJson(startingObj);
  }

  @Override
  public JsonObject solveIfNeeded(JsonObject obj) {
    Objects.requireNonNull(obj);
    if (isRef(obj)) {
      JsonObject o = getCached(JsonPointer.fromURI(URI.create(obj.getString("$ref"))));
      if (!o.equals(obj))
        return solveIfNeeded(o);
    }
    return obj;
  }

  private boolean isRef(JsonObject object) {
    return object.containsKey("$ref");
  }

  @Override
  public JsonObject getOpenAPI() {
    return openapiRoot;
  }

  public Map.Entry<JsonPointer, JsonObject> normalizeSchema(JsonObject schema, JsonPointer scope, Map<JsonPointer,
    JsonObject> additionalSchemasToRegister) {
    JsonObject normalized = schema.copy();
    JsonPointer newId = new SchemaURNId().toPointer();
    normalized.put("x-$id", newId.toURI().toString());

    innerNormalizeSchema(normalized, scope, additionalSchemasToRegister);
    return new AbstractMap.SimpleImmutableEntry<>(newId, normalized);
  }

  private void innerNormalizeSchema(Object schema, JsonPointer schemaRootScope,
                                    Map<JsonPointer, JsonObject> additionalSchemasToRegister) {
    if (schema instanceof JsonObject) {
      JsonObject schemaObject = (JsonObject) schema;
      if (isRef(schemaObject)) {
        // Ok so we found a $ref
        JsonPointer refPointer = JsonPointer.fromURI(URI.create(schemaObject.getString("$ref")));

        if (refPointer.isParent(schemaRootScope)) {
          // If it's a circular $ref, I need to remove $ref URI component and replace with newRef = scope - refPointer
          JsonPointer newRef = OpenAPI3Utils.pointerDifference(schemaRootScope, refPointer);
          schemaObject.put("$ref", newRef.toURI().toString());
        } else if (refPointer.equals(schemaRootScope)) {
          // If it's a circular $ref that points to schema root, I need to remove $ref URI component and replace with
          // newRef = scope - refPointer
          JsonPointer newRef = JsonPointer.create();
          schemaObject.put("$ref", newRef.toURI().toString());
        } else {
          // If it's not a circular $ref, I generate an id, I substitute the stuff and I register the schema to parse
          // Retrieve the cached schema
          JsonObject resolved = getCached(refPointer);
          if (!resolved.containsKey("x-$id")) { // This schema was never touched
            // And now the trick: I apply this function to this schema and then i register it in definitions
            JsonPointer id = new SchemaURNId().toPointer();
            resolved.put("x-$id", id.toURI().toString());

            // Now we make a copy of resolved to don't mess up original openapi json
            JsonObject resolvedCopy = resolved.copy();
            innerNormalizeSchema(resolvedCopy, refPointer, additionalSchemasToRegister);

            // Resolved it's ready to be consumed by the parser
            additionalSchemasToRegister.put(id, resolvedCopy);
          }
          // Substitute schema $ref with new one!
          schemaObject.put("$ref", resolved.getString("x-$id"));
        }
      } else {
        // Walk into the structure
        schemaObject.forEach(e -> innerNormalizeSchema(e.getValue(), schemaRootScope, additionalSchemasToRegister));
      }
    } else if (schema instanceof JsonArray) {
      // Walk into the structure
      JsonArray schemaArray = (JsonArray) schema;
      for (int i = 0; i < schemaArray.size(); i++) {
        innerNormalizeSchema(schemaArray.getValue(i), schemaRootScope, additionalSchemasToRegister);
      }
    }
  }

  protected URI getInitialScope() {
    return initialScope;
  }

  private Future<Void> walkAndSolve(JsonObject obj, URI scope) {
    List<JsonObject> candidateRefs = new ArrayList<>();
    Set<URI> refsToSolve = new HashSet<>();
    deepGetAllRefs(obj, candidateRefs);
    if (candidateRefs.isEmpty()) return Future.succeededFuture();

    for (JsonObject ref : candidateRefs) { // Make refs absolutes and check what refs must be solved
      JsonPointer parsedRef = JsonPointer.fromURI(URI.create(ref.getString("$ref")));
      if (!parsedRef.getURIWithoutFragment().isAbsolute()) // Ref not absolute, make it absolute based on scope
        parsedRef = JsonPointer.fromURI(
          URIUtils.replaceFragment(
            resolveRefResolutionURIWithoutFragment(parsedRef.getURIWithoutFragment(), scope),
            parsedRef.toURI().getFragment()
          )
        );
      URI solvedURI = parsedRef.toURI();
      ref.put("$ref", solvedURI.toString()); // Replace ref
      if (!absolutePaths.containsKey(parsedRef.getURIWithoutFragment()))
        refsToSolve.add(parsedRef.getURIWithoutFragment());
    }
    return CompositeFuture
      .all(refsToSolve.stream().map(this::resolveExternalRef).collect(Collectors.toList()))
      .compose(cf -> Future.succeededFuture());
  }

  private void deepGetAllRefs(Object obj, List<JsonObject> refsList) {
    if (obj instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) obj;
      if (jsonObject.containsKey("$ref"))
        refsList.add(jsonObject);
      else
        for (String keys : jsonObject.fieldNames()) deepGetAllRefs(jsonObject.getValue(keys), refsList);
    }
    if (obj instanceof JsonArray) {
      for (Object in : ((JsonArray) obj)) deepGetAllRefs(in, refsList);
    }
  }

  // We need this shitty substitution to get the validation working
  private void deepSubstituteForValidation(Object obj, JsonPointer scope,
                                           Map<JsonPointer, JsonPointer> originalToSubstitutedMap) {
    if (obj instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) obj;
      if (jsonObject.containsKey("$ref")) {
        JsonPointer pointer = JsonPointer.fromURI(URI.create(jsonObject.getString("$ref")));
        if (!pointer.isParent(scope)) { // Check circular refs hell!
          JsonObject cached = getCached(pointer);
          if (cached == null) {
            throw new IllegalStateException("Cannot resolve '" + pointer.toString() + "', this may be an invalid " +
              "reference");
          }
          if (!originalToSubstitutedMap.containsKey(pointer)) {
            JsonObject resolved = solveIfNeeded(cached).copy();
            jsonObject.remove("$ref");
            jsonObject.mergeIn(resolved);
            jsonObject.put("x-$ref", pointer.toURI().toString());
            originalToSubstitutedMap.put(pointer, scope);
            deepSubstituteForValidation(jsonObject, pointer, originalToSubstitutedMap);
          } else {
            JsonObject resolved = solveIfNeeded(cached).copy();
            jsonObject.remove("$ref");
            jsonObject.mergeIn(resolved);
            jsonObject.put("x-$ref", originalToSubstitutedMap.get(pointer).toURI().toString());
          }
        }
      } else
        for (String key : jsonObject.fieldNames())
          deepSubstituteForValidation(jsonObject.getValue(key), scope.copy().append(key), originalToSubstitutedMap);
    }
    if (obj instanceof JsonArray) {
      for (int i = 0; i < ((JsonArray) obj).size(); i++)
        deepSubstituteForValidation(
          ((JsonArray) obj).getValue(i),
          scope.copy().append(Integer.toString(i)),
          originalToSubstitutedMap
        );
    }
  }

  private Future<JsonObject> resolveExternalRef(final URI ref) {
    return externalSolvingRefs.computeIfAbsent(ref,
      uri ->
        ((URIUtils.isRemoteURI(uri)) ? solveRemoteRef(uri) : solveLocalRef(uri))
          .compose(j -> {
            absolutePaths.put(uri, j); // Circular refs hell!
            return walkAndSolve(j, uri).map(j);
          })

    );
  }

  private Future<JsonObject> solveRemoteRef(final URI ref) {
    String uri = ref.toString();
    if (!options.getAuthQueryParams().isEmpty()) {
      QueryStringEncoder encoder = new QueryStringEncoder(uri);
      options.getAuthQueryParams().forEach(encoder::addParam);
      uri = encoder.toString();
    }

    RequestOptions reqOptions = new RequestOptions()
      .setMethod(HttpMethod.GET)
      .setAbsoluteURI(uri)
      .setFollowRedirects(true)
      .addHeader(HttpHeaders.ACCEPT.toString(), "application/json, application/yaml, application/x-yaml");
    options.getAuthHeaders().forEach(reqOptions::addHeader);

    Promise<JsonObject> resultProm = Promise.promise();
    client.request(reqOptions, httpClientRequestAsyncResult -> {
      if (httpClientRequestAsyncResult.failed()) {
        resultProm.fail(httpClientRequestAsyncResult.cause());
        return;
      }

      httpClientRequestAsyncResult.result().send(responseAr -> {
        if (responseAr.failed()) {
          resultProm.fail(responseAr.cause());
          return;
        }

        HttpClientResponse res = responseAr.result();

        if (res.statusCode() != 200) {
          resultProm.fail(new IllegalStateException("Wrong status " + res.statusCode() + " " + res.statusMessage() + " received while resolving remote ref"));
          return;
        }

        boolean expectJson = "application/json".equals(res.getHeader("Content-Type"));

        res.bodyHandler(buf -> {
          try {
            if (expectJson) {
              resultProm.complete(buf.toJsonObject());
            } else {
              resultProm.complete(yamlToJson(buf));
            }
          } catch (DecodeException e) {
            resultProm.fail(new RuntimeException("Cannot decode the received " + (expectJson ? "JSON" : "YAML") + " response: ", e));
          }
        });
      });
    });

    return resultProm.future();
  }

  private Future<JsonObject> solveLocalRef(final URI ref) {
    String filePath = extractPath(ref);
    return fs.readFile(filePath).compose(buf -> {
      try {
        return Future.succeededFuture(buf.toJsonObject());
      } catch (DecodeException e) {
        // Maybe it's yaml
        try {
          return Future.succeededFuture(this.yamlToJson(buf));
        } catch (Exception e1) {
          return Future.failedFuture(new RuntimeException("File " + filePath + " is not a valid YAML or JSON", e1));
        }
      }
    });
  }

  private JsonObject yamlToJson(Buffer buf) {
    try {
      return JsonObject.mapFrom(yamlMapper.readTree(buf.getBytes()));
    } catch (IOException e) {
      throw new DecodeException("Cannot decode YAML", e);
    }
  }

  private URI resolveRefResolutionURIWithoutFragment(URI ref, URI scope) {
    if (ref.isAbsolute()) return URIUtils.removeFragment(ref);
    if (ref.getPath() != null && !ref.getPath().isEmpty() && !URIUtils.removeFragment(ref).equals(scope)) {
      if (ref.toString().startsWith(initialScopeDirectory))
        return URIUtils.removeFragment(ref);
      else
        return URIUtils.removeFragment(URIUtils.resolvePath(scope, ref.getPath()));
    }
    return scope;
  }

  private String extractPath(URI ref) {
    return ("jar".equals(ref.getScheme())) ? ref.getSchemeSpecificPart().split("!")[1].substring(1) : ref.getPath();
  }

  private Path uriToPath(URI uri) {
    try {
      switch (uri.getScheme()) {
        case "http":
          return Paths.get(uri.getPath());
        case "file":
          return Paths.get(uri);
        case "jar":
          return Paths.get(uri.getSchemeSpecificPart().split("!")[1].substring(1));
        default:
          throw new IllegalArgumentException("unsupported scheme type for '" + uri + "'");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to resolve path from " + uri, e);
    }
  }

  private String resolveContainingDirPath(URI absoluteURI) {
    return uriToPath(absoluteURI).resolveSibling("").toString();
  }

  protected static URI getResourceAbsoluteURI(URI relativeURI) {
    // If it's relative, it could be both in filesystem and in the classpath.
    // Try to figure this out!
    URI fromClasspath = getResourceAbsoluteURIFromClasspath(relativeURI);
    if (fromClasspath != null) {
      return fromClasspath;
    } else {
      // Then it must be local fs!
      return Paths.get(relativeURI.getPath()).toAbsolutePath().toUri();
    }
  }

  protected static URI getResourceAbsoluteURIFromClasspath(URI u) {
    try {
      return getClassLoader().getResource(u.toString()).toURI();
    } catch (NullPointerException | URISyntaxException e) {
      return null;
    }
  }

  private static ClassLoader getClassLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = OpenAPIHolderImpl.class.getClassLoader();
    }
    // when running on substratevm (graal) the access to class loaders
    // is very limited and might be only available from compile time
    // known classes. (Object is always known, so we do a final attempt
    // to get it here).
    if (cl == null) {
      cl = Object.class.getClassLoader();
    }
    return cl;
  }

  public Map<URI, JsonObject> getAbsolutePaths() {
    return absolutePaths;
  }
}
