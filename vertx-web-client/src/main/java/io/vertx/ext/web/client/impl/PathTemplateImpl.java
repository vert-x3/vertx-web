package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.PathTemplate;
import io.vertx.ext.web.client.UriParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PathTemplateImpl implements PathTemplate {

  List<Function<UriParameters, String>> templateChunksGenerators;

  private PathTemplateImpl(List<Function<UriParameters, String>> templateChunksGenerators) {
    this.templateChunksGenerators = templateChunksGenerators;
  }

  @Override
  public String expand(UriParameters parameters) {
    return templateChunksGenerators.stream().map(f -> f.apply(parameters)).collect(Collectors.joining());
  }

  public static PathTemplateImpl parse(String pathTemplate) {
    if (pathTemplate.isEmpty()) return new PathTemplateImpl(new ArrayList<>());
    if (!pathTemplate.startsWith("/")) throw new IllegalArgumentException("Path template must start with /");

    List<Function<UriParameters, String>> templateChunksGenerators = new ArrayList<>();
    String tempPathTemplate = (pathTemplate.contains("?")) ?
      pathTemplate.substring(0, pathTemplate.lastIndexOf("?")) : pathTemplate;

    while(!tempPathTemplate.isEmpty()) {
      int firstColon = tempPathTemplate.indexOf(':');
      if (firstColon == -1) { // No more params
        final String lastChunk = tempPathTemplate;
        templateChunksGenerators.add(f -> lastChunk);
        break;
      } else {
        String chunkBeforeParameter = pathTemplate.substring(0, firstColon);
        templateChunksGenerators.add(p -> chunkBeforeParameter);
        tempPathTemplate = tempPathTemplate.substring(firstColon + 1);

        //Now let's handle the param
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < tempPathTemplate.length()) {
          char extracted = tempPathTemplate.charAt(index);
          if (extracted == '/' || extracted == ':') break;
          else {
            builder.append(extracted);
            index++;
          }
        }
        final String paramName = builder.toString();
        templateChunksGenerators.add(f -> {
          if (f.getEscapedParam(paramName) == null) throw new IllegalArgumentException("Missing path parameter " + paramName);
          else return String.join("/", f.getEscapedParam(paramName));
        });
        tempPathTemplate = tempPathTemplate.substring(index);
      }
    }

    if (pathTemplate.contains("?")) {
      final String queryString = pathTemplate.substring(pathTemplate.lastIndexOf("?"));
      templateChunksGenerators.add(f -> queryString);
    }

    return new PathTemplateImpl(templateChunksGenerators);
  }
}
