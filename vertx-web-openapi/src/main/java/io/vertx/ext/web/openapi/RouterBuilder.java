package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.impl.OpenAPI3RouterBuilderImpl;
import io.vertx.ext.web.openapi.impl.OpenAPIHolderImpl;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.ValidationException;

import java.util.List;
import java.util.function.Function;

/**
 * Interface to build a Vert.x Web {@link Router} from an OpenAPI 3 contract. <br/>
 * To add an handler, use {@link RouterBuilder#operation(String)} (String, Handler)}<br/>
 * Usage example:
 * <pre>
 * {@code
 * RouterBuilder.create(vertx, "src/resources/spec.yaml", asyncResult -> {
 *  if (!asyncResult.succeeded()) {
 *     // IO failure or spec invalid
 *  } else {
 *     RouterBuilder routerBuilder = asyncResult.result();
 *     RouterBuilder.operation("operation_id").handler(routingContext -> {
 *        // Do something
 *     }, routingContext -> {
 *        // Do something with failure handler
 *     });
 *     Router router = routerBuilder.createRouter();
 *  }
 * });
 * }
 * </pre>
 * <br/>
 * Handlers are loaded in this order:<br/>
 *  <ol>
 *   <li>Body handler (Customizable with {@link this#bodyHandler(BodyHandler)}</li>
 *   <li>Custom global handlers configurable with {@link this#rootHandler(Handler)}</li>
 *   <li>Global security handlers defined in upper spec level</li>
 *   <li>Operation specific security handlers</li>
 *   <li>Generated validation handler</li>
 *   <li>User handlers or "Not implemented" handler</li>
 * </ol>
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RouterBuilder {

  /**
   * Access to an operation defined in the contract with {@code operationId}
   *
   * @param operationId the id of the operation
   * @return the requested operation
   * @throws IllegalArgumentException if the operation id doesn't exist in the contract
   */
  @Nullable Operation operation(String operationId);

  /**
   * @return all operations defined in the contract
   */
  List<Operation> operations();

  /**
   * Supply your own BodyHandler if you would like to control body limit, uploads directory and deletion of uploaded
   * files.
   * If you provide a null body handler, you won't be able to validate request bodies
   *
   * @param bodyHandler
   * @return self
   *
   * @deprecated Use {@link #rootHandler(Handler)} instead. The order matters, so adding the body handler should
   * happen after any {@code PLATFORM} or {@code SECURITY_POLICY} handler(s).
   */
  @Fluent
  @Deprecated
  default RouterBuilder bodyHandler(@Nullable BodyHandler bodyHandler) {
    return rootHandler(bodyHandler);
  }

  /**
   * Add global handler to be applied prior to {@link Router} being generated. <br/>
   * Please note that you should not add a body handler inside that list. If you want to modify the body handler,
   * please use {@link RouterBuilder#bodyHandler(BodyHandler)}
   *
   * @param rootHandler
   * @return self
   */
  @Fluent
  RouterBuilder rootHandler(Handler<RoutingContext> rootHandler);

  /**
   * Mount to paths that have to follow a security schema a security handler. This method will not perform any
   * validation weather or not the given {@code securitySchemeName} is present in the OpenAPI document.
   *
   * For must use cases the method {@link #securityHandler(String)} should be used.
   *
   * @param securitySchemeName the components security scheme id
   * @param handler the authentication handler
   * @return self
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  RouterBuilder securityHandler(String securitySchemeName, AuthenticationHandler handler);

  /**
   * Introspect the OpenAPI spec to mount handlers for all operations that specifies a x-vertx-event-bus annotation.
   * Please give a look at
   * <a href="https://vertx.io/docs/vertx-web-api-service/java/">vertx-web-api-service documentation</a>
   * for more informations
   *
   * @return self
   */
  @Fluent
  RouterBuilder mountServicesFromExtensions();

  /**
   * Introspect the Web Api Service interface to route to service all matching method names with operation ids.
   * Please give a look at
   * <a href="https://vertx.io/docs/vertx-web-api-service/java/">vertx-web-api-service documentation</a>
   * for more informations
   *
   * @return self
   */
  @Fluent
  @GenIgnore
  RouterBuilder mountServiceInterface(Class interfaceClass, String address);

  /**
   * Set options of router builder. For more info {@link RouterBuilderOptions}
   *
   * @param options
   * @return self
   */
  @Fluent
  RouterBuilder setOptions(RouterBuilderOptions options);

  /**
   * @return options of router builder. For more info {@link RouterBuilderOptions}
   */
  RouterBuilderOptions getOptions();

  /**
   * @return holder used by self to process the OpenAPI. You can use it to resolve {@code $ref}s
   */
  OpenAPIHolder getOpenAPI();

  /**
   * @deprecated This method exposes the internal of the OpenAPI handler, it will be removed in the future. Users should
   * configure the json schema module from the options.
   *
   * @return schema router used by self to internally manage all {@link io.vertx.json.schema.Schema} instances
   */
  @Deprecated
  SchemaRouter getSchemaRouter();

  /**
   * @deprecated This method exposes the internal of the OpenAPI handler, it will be removed in the future. Users should
   * configure the json schema module from the options.
   *
   * @return schema parser used by self to parse all {@link io.vertx.json.schema.Schema}
   */
  @Deprecated
  SchemaParser getSchemaParser();

  /**
   * When set, this function is called while creating the payload of {@link io.vertx.ext.web.api.service.ServiceRequest}
   *
   * @param serviceExtraPayloadMapper
   * @return self
   */
  @Fluent
  RouterBuilder serviceExtraPayloadMapper(Function<RoutingContext, JsonObject> serviceExtraPayloadMapper);

  /**
   * Creates a new security scheme for the required {@link AuthenticationHandler}.
   * @return a security scheme.
   */
  SecurityScheme securityHandler(String securitySchemeName);

  /**
   * Construct a new router based on spec. It will fail if you are trying to mount a spec with security schemes
   * without assigned handlers<br/>
   *
   * <b>Note:</b> Router is built when this function is called and the path definition ordering in contract is respected.
   *
   * @return
   */
  Router createRouter();

  /**
   * Create a new {@link RouterBuilder}
   *
   * @param vertx
   * @param url   location of your spec. It can be an absolute path, a local path or remote url (with HTTP/HTTPS
   *              protocol)
   * @return Future completed with success when specification is loaded and valid
   */
  static Future<RouterBuilder> create(Vertx vertx, String url) {
    return create(vertx, url, new OpenAPILoaderOptions());
  }

  /**
   * Like {@link this#create(Vertx, String)}
   */
  static void create(Vertx vertx, String url, Handler<AsyncResult<RouterBuilder>> handler) {
    RouterBuilder.create(vertx, url).onComplete(handler);
  }

  /**
   * Create a new {@link RouterBuilder}
   *
   * @param vertx
   * @param url     location of your spec. It can be an absolute path, a local path or remote url (with HTTP/HTTPS
   *                protocol)
   * @param options options for specification loading
   * @return Future completed with success when specification is loaded and valid
   */
  static Future<RouterBuilder> create(Vertx vertx,
                                      String url,
                                      OpenAPILoaderOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    Promise<RouterBuilder> promise = ctx.promise();

    HttpClient httpClient = vertx.createHttpClient();
    OpenAPIHolderImpl loader = new OpenAPIHolderImpl(vertx, httpClient, vertx.fileSystem(), options);
    loader.loadOpenAPI(url).onComplete(ar -> {
      if (ar.failed()) {
        if (ar.cause() instanceof ValidationException) {
          promise.fail(RouterBuilderException.createInvalidSpec(ar.cause()));
        } else {
          promise.fail(RouterBuilderException.createInvalidSpecFile(url, ar.cause()));
        }
      } else {
        RouterBuilder factory;
        try {
          factory = new OpenAPI3RouterBuilderImpl(vertx, httpClient, loader, options);
        } catch (Exception e) {
          promise.fail(RouterBuilderException.createRouterBuilderInstantiationError(e, url));
          return;
        }
        promise.complete(factory);
      }
    });

    return promise.future();
  }

  /**
   * Like {@link this#create(Vertx, String, OpenAPILoaderOptions)}
   */
  static void create(Vertx vertx,
                     String url,
                     OpenAPILoaderOptions options,
                     Handler<AsyncResult<RouterBuilder>> handler) {
    RouterBuilder.create(vertx, url, options).onComplete(handler);
  }

}
