package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.vertx.ext.web.api.contract.RouterFactoryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3PathResolver {

  String oasPath;
  List<Parameter> parameters;

  Pattern resolvedPattern;
  //Key is new name, value is old name
  Map<String, String> mappedGroups;

  public static final Pattern OAS_PATH_PARAMETERS_PATTERN = Pattern.compile("\\{{1}[.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*\\}{1}");
  public static final Pattern ILLEGAL_PATH_MATCHER = Pattern.compile("\\{[^\\/]*\\/[^\\/]*\\}");

  public static final String QUERY_REGEX_WITH_SLASH = "\\/?(?>\\??[^\\/]*)?";
  public static final String QUERY_REGEX_WITHOUT_SLASH = "(?>\\??[^\\/]*)?";

  public OpenAPI3PathResolver(String oasPath, List<Parameter> parameters) {
    this.oasPath = oasPath;
    this.parameters = parameters;

    this.mappedGroups = new HashMap<>();
  }

  public Pattern solve() {
    // Filter parameters to get only path parameters
    parameters = parameters.stream().filter(parameter -> parameter.getIn().equals("path")).collect(Collectors.toList());

    if (ILLEGAL_PATH_MATCHER.matcher(oasPath).matches())
      throw new RouterFactoryException("Path template not supported", RouterFactoryException.ErrorType.INVALID_SPEC_PATH);

    Matcher parametersMatcher = OAS_PATH_PARAMETERS_PATTERN.matcher(oasPath);

    if (!parameters.isEmpty() && parametersMatcher.find()) {
      // Need to create a specific match pattern with path parameters and some string manipulation magic
      StringBuilder regex = new StringBuilder();
      int lastMatchEnd = 0;
      boolean endSlash = oasPath.charAt(oasPath.length() - 1) == '/';
      parametersMatcher.reset();
      int i = 0;
      while (parametersMatcher.find()) {
        // Append constant string
        regex.append(Pattern.quote(oasPath.substring(lastMatchEnd, parametersMatcher.start())));
        lastMatchEnd = parametersMatcher.end();

        String paramName = parametersMatcher.group(1);
        Optional<Parameter> parameterOptional = parameters.stream().filter(p -> p.getName().equals(paramName)).findFirst();
        if (parameterOptional.isPresent()) {
          // For every parameter style I have to generate a different regular expression
          Parameter parameter = parameterOptional.get();
          String style = (parameter.getStyle() != null) ? parameter.getStyle().toString() : "simple";
          boolean explode = (parameter.getExplode() != null) ? parameter.getExplode() : false;
          boolean isObject = OpenApi3Utils.isParameterObjectOrAllOfType(parameter);
          boolean isArray = OpenApi3Utils.isParameterArrayType(parameter);

          String groupName = "p" + i;

          /*
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | style  | explode | empty  | string      | array                               | object                   |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | matrix | false   | ;color | ;color=blue | ;color=blue,black,brown             | ;color=R,100,G,200,B,150 |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | matrix | true    | ;color | ;color=blue | ;color=blue;color=black;color=brown | ;R=100;G=200;B=150       |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | label  | false   | .      | .blue       | .blue.black.brown                   | .R.100.G.200.B.150       |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | label  | true    | .      | .blue       | .blue.black.brown                   | .R=100.G=200.B=150       |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | simple | false   | n/a    | blue        | blue,black,brown                    | R,100,G,200,B,150        |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
            | simple | true    | n/a    | blue        | blue,black,brown                    | R=100,G=200,B=150        |
            +--------+---------+--------+-------------+-------------------------------------+--------------------------+
           */

          switch (style) {
            case "simple":
              regex.append("(?<" + groupName + ">[^\\/\\;\\?\\:\\@\\&\\.\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)?");
              mappedGroups.put(groupName, paramName);
              break;
            case "label":
              if (isObject && explode) {
                Map<String, OpenApi3Utils.ObjectField> properties = OpenApi3Utils.solveObjectParameters(parameter.getSchema());
                for (Map.Entry<String, OpenApi3Utils.ObjectField> entry : properties.entrySet()) {
                  groupName = "p" + i;
                  regex.append("\\.?" + entry.getKey() + "=(?<" + groupName + ">[^\\/\\;\\?\\:\\@\\=\\.\\&\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)");
                  mappedGroups.put(groupName, entry.getKey());
                  i++;
                }
              } else {
                regex.append("\\.?(?<" + groupName + ">[^\\/\\;\\?\\:\\@\\=\\&\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)?");
                mappedGroups.put(groupName, paramName);
              }
              break;
            case "matrix":
              if (isObject && explode) {
                Map<String, OpenApi3Utils.ObjectField> properties = OpenApi3Utils.solveObjectParameters(parameter.getSchema());
                for (Map.Entry<String, OpenApi3Utils.ObjectField> entry : properties.entrySet()) {
                  groupName = "p" + i;
                  regex.append("\\;" + entry.getKey() + "=(?<" + groupName + ">[^\\/\\;\\?\\:\\@\\=\\.\\&\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)");
                  mappedGroups.put(groupName, entry.getKey());
                  i++;
                }
              } else if (isArray && explode) {
                regex.append("(?<" + groupName + ">(?>;" + paramName + "=[^\\/\\;\\?\\:\\@\\&\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)+)");
                mappedGroups.put(groupName, paramName);
              } else {
                regex.append(";" + paramName + "=(?<" + groupName + ">[^\\/\\;\\?\\:\\@\\=\\&\\\"\\<\\>\\#\\{\\}\\|\\\\\\^\\~\\[\\]\\`]*)?");
                mappedGroups.put(groupName, paramName);
              }
              break;
          }
        } else {
          throw RouterFactoryException.createSpecInvalidException("Missing parameter description for parameter name: " + paramName);
        }
        i++;
      }
      regex.append(Pattern.quote(oasPath.substring(lastMatchEnd, (endSlash) ? oasPath.length() - 1 : oasPath.length())));
      if (endSlash)
        regex.append(QUERY_REGEX_WITH_SLASH);
      else
        regex.append(QUERY_REGEX_WITHOUT_SLASH);
      resolvedPattern = Pattern.compile(regex.toString());
    } else {
      resolvedPattern = Pattern.compile(Pattern.quote(oasPath));
    }
    return resolvedPattern;
  }

  public Pattern getResolvedPattern() {
    return resolvedPattern;
  }

  public Map<String, String> getMappedGroups() {
    return mappedGroups;
  }
}
