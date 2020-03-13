package io.vertx.ext.web.openapi.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.common.SchemaURNId;
import io.vertx.ext.json.schema.common.URIUtils;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OpenAPIHolderImpl implements OpenAPIHolder {

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

  private static URI openapiSchemaURI;
  private static JsonObject openapiSchemaJson;

  static {
    try {
      openapiSchemaURI = OpenAPIHolderImpl.class.getResource("/openapi_3_schema.json").toURI();
      openapiSchemaJson = new JsonObject(
          String.join("",
              Files.readAllLines(Paths.get(openapiSchemaURI))
          )
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public OpenAPIHolderImpl(HttpClient client, FileSystem fs, OpenAPILoaderOptions options) {
    absolutePaths = new ConcurrentHashMap<>();
    externalSolvingRefs = new ConcurrentHashMap<>();
    this.client = client;
    this.fs = fs;
    this.options = options;
    this.router = SchemaRouter.create(client, fs, options.toSchemaRouterOptions());
    this.parser = Draft7SchemaParser.create(this.router);
    this.yamlMapper = new YAMLMapper();
    this.openapiSchema = parser.parse(openapiSchemaJson, JsonPointer.fromURI(openapiSchemaURI));
  }

  public Future<JsonObject> loadOpenAPI(String u) {
    URI uri = URIUtils.removeFragment(URI.create(u));
    Future<JsonObject> resolvedOpenAPIDocumentUnparsed = (URIUtils.isRemoteURI(uri)) ? solveRemoteRef(uri) : solveLocalRef(uri);
    initialScope = (URIUtils.isRemoteURI(uri)) ? uri : URI.create(sanitizeLocalRef(uri));
    initialScopeDirectory = Paths.get(initialScope.getPath()).resolveSibling("").toString();
    return resolvedOpenAPIDocumentUnparsed
        .compose(openapi -> {
          absolutePaths.put(initialScope, openapi); // Circular refs hell!
          openapiRoot = openapi;
          return walkAndSolve(openapi, initialScope).map(openapi);
        })
        .compose(openapi -> {
          JsonObject openapiCopy = openapi.copy();
          deepSubstituteForValidation(openapiCopy, JsonPointer.fromURI(initialScope), new HashMap<>());
          // We need this shitty flattened spec just to validate it
          return openapiSchema.validateAsync(openapiCopy).map(openapi);
        });
  }

  @Override
  public JsonObject getCached(JsonPointer pointer) {
    JsonObject startingObj = absolutePaths.get(resolveRefResolutionURIWithoutFragment(pointer.getURIWithoutFragment(), initialScope));
    return (JsonObject) pointer.queryJson(startingObj);
  }

  @Override
  public JsonObject solveIfNeeded(JsonObject obj) {
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

  public Map.Entry<JsonPointer, JsonObject> normalizeSchema(JsonObject schema, JsonPointer scope, Map<JsonPointer, JsonObject> additionalSchemasToRegister) {
    JsonObject normalized = schema.copy();
    JsonPointer newId = new SchemaURNId().toPointer();
    normalized.put("x-$id", newId.toURI().toString());

    innerNormalizeSchema(normalized, scope, additionalSchemasToRegister);
    return new AbstractMap.SimpleImmutableEntry<>(newId, normalized);
  }

  private void innerNormalizeSchema(Object schema, JsonPointer schemaRootScope, Map<JsonPointer, JsonObject> additionalSchemasToRegister) {
    if (schema instanceof JsonObject) {
      JsonObject schemaObject = (JsonObject) schema;
      if (isRef(schemaObject)) {
        // Ok so we found a $ref
        JsonPointer refPointer = JsonPointer.fromURI(URI.create(schemaObject.getString("$ref")));

        if (refPointer.isParent(schemaRootScope)) {
          // If it's a circular $ref, I need to remove $ref URI component and replace with newRef = scope - refPointer
          JsonPointer newRef = OpenApi3Utils.pointerDifference(schemaRootScope, refPointer);
          schemaObject.put("$ref", newRef.toURI().toString());
        } else if (refPointer.equals(schemaRootScope)) {
          // If it's a circular $ref that points to schema root, I need to remove $ref URI component and replace with newRef = scope - refPointer
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
                resolveRefResolutionURIWithoutFragment(parsedRef.getURIWithoutFragment(), scope), parsedRef.toURI().getFragment()
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
  private void deepSubstituteForValidation(Object obj, JsonPointer scope, Map<JsonPointer, JsonPointer> originalToSubstitutedMap) {
    if (obj instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) obj;
      if (jsonObject.containsKey("$ref")) {
        JsonPointer pointer = JsonPointer.fromURI(URI.create(jsonObject.getString("$ref")));
        if (!pointer.isParent(scope)) { // Check circular refs hell!
          if (!originalToSubstitutedMap.containsKey(pointer)) {
            JsonObject resolved = solveIfNeeded(getCached(pointer)).copy();
            jsonObject.remove("$ref");
            jsonObject.mergeIn(resolved);
            jsonObject.put("x-$ref", pointer.toURI().toString());
            originalToSubstitutedMap.put(pointer, scope);
            deepSubstituteForValidation(jsonObject, pointer, originalToSubstitutedMap);
          } else {
            JsonObject resolved = solveIfNeeded(getCached(pointer)).copy();
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
      for (int i = 0; i < ((JsonArray)obj).size(); i++)
        deepSubstituteForValidation(
          ((JsonArray)obj).getValue(i),
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

    return client.get(reqOptions).compose(res -> {
      if (res.statusCode() != 200) {
        return Future.failedFuture(new IllegalStateException("Wrong status " + res.statusCode() + " " + res.statusMessage() + " received while resolving remote ref"));
      }

      String contentType = res.getHeader("Content-Type");
      if ("application/json".equals(contentType)) {
        return res.body().compose(buf -> {
          try {
            return Future.succeededFuture(buf.toJsonObject());
          } catch (DecodeException e) {
            return Future.failedFuture(new RuntimeException("Cannot decode the received Json Response: ", e));
          }
        });
      } else {
        return res.body().compose(buf -> {
          try {
            return Future.succeededFuture(yamlToJson(buf));
          } catch (DecodeException e) {
            return Future.failedFuture(new RuntimeException("Cannot decode the received Json Response: ", e));
          }
        });
      }
    });
  }

  private Future<JsonObject> solveLocalRef(final URI ref) {
    String filePath = sanitizeLocalRef(ref);
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
      throw new RuntimeException("Cannot decode YAML", e);
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

  private String sanitizeLocalRef(URI ref) {
    return ("jar".equals(ref.getScheme())) ? ref.getSchemeSpecificPart().split("!")[1].substring(1) : ref.getPath();
  }

  public Map<URI, JsonObject> getAbsolutePaths() {
    return absolutePaths;
  }
}
