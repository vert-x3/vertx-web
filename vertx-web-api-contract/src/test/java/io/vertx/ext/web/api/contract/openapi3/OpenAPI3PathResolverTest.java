package io.vertx.ext.web.api.contract.openapi3;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3PathResolver;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAPI3PathResolverTest {

  OpenAPI testSpec;

  @Rule
  public ExternalResource res = new ExternalResource() {
    private OpenAPI loadSwagger(String filename) {
      return new OpenAPIV3Parser().readLocation(filename, null, OpenApi3Utils.getParseOptions()).getOpenAPI();
    }

    @Override
    protected void before() {
      testSpec = loadSwagger("src/test/resources/swaggers/path_resolver_test.yaml");
    }

    @Override
    protected void after() { }

  };

  private Operation getOperation(String operationId) {
    return testSpec
      .getPaths()
      .values()
      .stream()
      .flatMap(e -> e.readOperations().stream())
      .filter(e -> e.getOperationId().equals(operationId))
      .findFirst().orElse(null);
  }

  private String getPath(String operationId) {
    return testSpec
      .getPaths()
      .entrySet()
      .stream()
      .flatMap(e -> e
        .getValue()
        .readOperations()
        .stream().map(e2 -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e2)))
      .filter(e -> e.getValue().getOperationId().equals(operationId))
      .map(e -> e.getKey())
      .findFirst().orElse(null);
  }

  private OpenAPI3PathResolver instantiatePathResolver(String operationId){
    return new OpenAPI3PathResolver(getPath(operationId), getOperation(operationId).getParameters());
  }

  private void shouldMatchParameter(OpenAPI3PathResolver resolver, String path, String parameterName, String parameterValue) {
    Optional<Pattern> optional = resolver.solve();
    assertTrue(optional.isPresent());
    Pattern p = optional.get();
    Matcher m = p.matcher(path);
    assertTrue(m.lookingAt());
    String value = m.group(resolver.getMappedGroups().entrySet().stream().filter(e -> e.getValue().equals(parameterName)).map(Map.Entry::getKey).findFirst().get());
    String decoded = decode(value);
    assertEquals(parameterValue, decoded);
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
    assertFalse(resolver.solve().isPresent());
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
    assertFalse(pattern.contains("\\Q\\E"));
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
