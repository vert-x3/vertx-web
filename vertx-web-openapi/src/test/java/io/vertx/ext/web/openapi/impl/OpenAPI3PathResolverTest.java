package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class OpenAPI3PathResolverTest {

  OpenAPIHolderImpl loader;
  JsonObject openapi;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext context) {
    loader = new OpenAPIHolderImpl(vertx, vertx.createHttpClient(), vertx.fileSystem(), new OpenAPILoaderOptions());
    loader
      .loadOpenAPI("src/test/resources/specs/path_resolver_test.yaml")
      .onComplete(ar -> {
        if (ar.succeeded()) {
          openapi = loader.getOpenAPI();
          context.completeNow();
        } else context.failNow(ar.cause());
      });
  }

  private JsonObject getOperation(String operationId) {
    return openapi
      .getJsonObject("paths")
      .stream()
      .map(Map.Entry::getValue)
      .map(v -> (JsonObject)v)
      .flatMap(j -> j
        .stream()
        .map(Map.Entry::getValue)
        .map(v -> (JsonObject)v)
      )
      .filter(j -> j.getString("operationId").equals(operationId))
      .findFirst()
      .orElse(null);
  }

  private String getPath(String operationId) {
    return openapi
      .getJsonObject("paths")
      .stream()
      .flatMap(j -> ((JsonObject)j.getValue())
        .stream()
        .map(Map.Entry::getValue)
        .map(v -> new SimpleImmutableEntry<>(j.getKey(), (JsonObject)v))
      )
      .filter(e -> e.getValue().getString("operationId").equals(operationId))
      .map(SimpleImmutableEntry::getKey)
      .findFirst()
      .orElse(null);
  }

  private OpenAPI3PathResolver instantiatePathResolver(String operationId){
    return new OpenAPI3PathResolver(
      getPath(operationId),
      getOperation(operationId)
        .getJsonArray("parameters", new JsonArray())
        .stream()
        .map(o -> (JsonObject)o)
        .collect(Collectors.toList()),
      loader
    );
  }

  private void shouldMatchParameter(OpenAPI3PathResolver resolver, String path, String parameterName, String parameterValue) {
    Optional<Pattern> optional = resolver.solve();
    assertThat(optional.isPresent()).isTrue();
    Pattern p = optional.get();
    Matcher m = p.matcher(path);
    assertThat(m.lookingAt()).isTrue();
    String value = m.group(resolver.getMappedGroups().entrySet().stream().filter(e -> e.getValue().equals(parameterName)).map(Map.Entry::getKey).findFirst().get());
    String decoded = decode(value);
    assertThat(decoded).isEqualTo(parameterValue);
  }

  private String encode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8")
        .replaceAll("\\.", "%2E"); //Dot should be encoded!
    } catch (Exception e) {
      return null;
    }
  }

  private String decode(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (Exception e) {
      return null;
    }
  }

  @Test
  public void shouldNotGenerateRegex() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("listPets");
    assertThat(resolver.solve().isPresent()).isFalse();
  }

  @Test
  public void complexEncodingMultiSimpleLabelMixed() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_multi_simple_label");
    String path = "/path/multi/" + encode("admin@vertx.io") + "." + encode("user@vertx.io") + "." + encode("committer@vertx.io") + "/test";
    shouldMatchParameter(
      resolver,
      path,
      "color_simple",
      "admin@vertx.io");
    shouldMatchParameter(
      resolver,
      path,
      "color_label",
      "user@vertx.io.committer@vertx.io");
    Optional<Pattern> optional = resolver.solve();
    Pattern p = optional.get();
  }

  @Test
  public void shouldNotHaveEmptyStringQuoting() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_multi_simple_label");
    Optional<Pattern> optional = resolver.solve();
    Pattern p = optional.get();
    String pattern = p.toString();
    assertThat(pattern.contains("\\Q\\E")).isFalse();
  }

  @Test
  public void complexMatrixArrayNotExploded() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_array_matrix");
    String path = "/path/;matrix=" + encode("admin@vertx.io") + "," + encode("user@vertx.io") + "," + encode("committer@vertx.io") + "/test";
    shouldMatchParameter(
      resolver,
      path,
      "matrix",
      "admin@vertx.io,user@vertx.io,committer@vertx.io");
    String withoutDotEncoded = "/path/;matrix=" + encode("admin@vertx") + ".io," + encode("user@vertx") + ".io," + encode("committer@vertx") + ".io/test";
    shouldMatchParameter(
      resolver,
      withoutDotEncoded,
      "matrix",
      "admin@vertx.io,user@vertx.io,committer@vertx.io");
  }


  @Test
  public void dotInASimplePathParam() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_simple");
    String path = "/path/bla.bla.bla/test";
    shouldMatchParameter(
      resolver,
      path,
      "simple",
      "bla.bla.bla"
    );
  }

  @Test
  public void semicolonInASimplePathParam() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_simple");
    String path = "/path/bla:bla:bla/test";
    shouldMatchParameter(
      resolver,
      path,
      "simple",
      "bla:bla:bla"
    );
  }

  @Test
  public void matrixWithSemicolon() {
    OpenAPI3PathResolver resolver = instantiatePathResolver("path_matrix_id_email");
    String path = "/path/;id=" + "bla:bla" + ";email=" + encode("user@vertx.io") + "/test";
    shouldMatchParameter(
      resolver,
      path,
      "id",
      "bla:bla");
    shouldMatchParameter(
      resolver,
      path,
      "email",
      "user@vertx.io");
  }

}
