package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.vertx.ext.web.api.contract.RouterFactoryException;

import java.util.*;
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

  private boolean shouldThreatDotAsReserved;

  public OpenAPI3PathResolver(String oasPath, List<Parameter> parameters) {
    this.oasPath = oasPath;

    // Filter parameters to get only path parameters
    if (parameters != null)
      this.parameters = parameters.stream().filter(parameter -> parameter.getIn().equals("path")).collect(Collectors.toList());
    else
      this.parameters = new ArrayList<>();

    // If there's a parameter with label style, the dot should be escaped to avoid conflicts
    this.shouldThreatDotAsReserved = hasParameterWithStyle("label");

    this.mappedGroups = new HashMap<>();
  }

  /**
   * This method returns a pattern only if a pattern is needed, otherwise it returns an empty optional
   *
   * @return
   */
  public Optional<Pattern> solve() {
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
        String toQuote = oasPath.substring(lastMatchEnd, parametersMatcher.start());
        if (toQuote.length() != 0)
          regex.append(Pattern.quote(toQuote));
        lastMatchEnd = parametersMatcher.end();

        String paramName = parametersMatcher.group(1);
        Optional<Parameter> parameterOptional = parameters.stream().filter(p -> p.getName().equals(paramName)).findFirst();
        if (parameterOptional.isPresent()) {
          // For every parameter style I have to generate a different regular expression
          Parameter parameter = parameterOptional.get();
          String style = solveParamStyle(parameter);
          boolean explode = solveParamExplode(parameter);
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

            RFC 3986 section 2.2 Reserved Characters (January 2005)
            !	*	'	(	)	;	:	@	&	=	+	$	,	/	?	#	[	]

           */

          if (style.equals("simple")) {
            regex.append(
              RegexBuilder.create().namedGroup(
                groupName,
                RegexBuilder.create().notCharactersClass(
                  "!",	"*",	"'", "(",	")",	";",	"@",	"&",	"+",	"$",	"/",	"?",	"#",	"[",	"]", (shouldThreatDotAsReserved) ? "." : null
                ).zeroOrMore()
              ).zeroOrOne()
            );
            mappedGroups.put(groupName, paramName);
          } else if (style.equals("label")) {
            if (isObject && explode) {
              Map<String, OpenApi3Utils.ObjectField> properties = OpenApi3Utils.solveObjectParameters(parameter.getSchema());
              for (Map.Entry<String, OpenApi3Utils.ObjectField> entry : properties.entrySet()) {
                groupName = "p" + i;
                regex.append(
                  RegexBuilder.create().optionalGroup(
                    RegexBuilder.create()
                      .escapeCharacter(".").zeroOrOne().quote(entry.getKey()).append("=")
                      .namedGroup(groupName,
                        RegexBuilder.create().notCharactersClass(
                          "!",	"*",	"'", "(",	")",	";",	"@",	"&",	"+",	"$",	"/",	"?",	"#",	"[",	"]", ".", "="
                        ).zeroOrMore()
                      )
                  )
                );
                mappedGroups.put(groupName, entry.getKey());
                i++;
              }
            } else {
              regex.append(
                RegexBuilder.create()
                  .escapeCharacter(".").zeroOrOne()
                  .namedGroup(groupName,
                    RegexBuilder.create().notCharactersClass(
                      "!",	"*",	"'",	"(",	")",	";",	"@",	"&",	"=",	"+",	"$",	",",	"/",	"?",	"#",	"[",	"]"
                    ).zeroOrMore()
                  ).zeroOrOne()
              );
              mappedGroups.put(groupName, paramName);
            }
          } else if (style.equals("matrix")) {
            if (isObject && explode) {
              Map<String, OpenApi3Utils.ObjectField> properties = OpenApi3Utils.solveObjectParameters(parameter.getSchema());
              for (Map.Entry<String, OpenApi3Utils.ObjectField> entry : properties.entrySet()) {
                groupName = "p" + i;
                regex.append(
                  RegexBuilder.create().optionalGroup(
                    RegexBuilder.create()
                      .escapeCharacter(";").quote(entry.getKey()).append("=")
                      .namedGroup(groupName,
                        RegexBuilder.create().notCharactersClass(
                          "!",	"*",	"'",	"(",	")",	";",	"@",	"&",	"=",	"+",	"$",	",",	"/",	"?",	"#",	"[",	"]",
                          (shouldThreatDotAsReserved) ? "." : null
                        ).zeroOrMore()
                      )
                  )
                );
                mappedGroups.put(groupName, entry.getKey());
                i++;
              }
            } else if (isArray && explode) {
              regex.append(
                RegexBuilder.create().namedGroup(
                  groupName,
                  RegexBuilder.create().atomicGroup(
                    RegexBuilder.create()
                      .append(";").quote(paramName).append("=")
                      .notCharactersClass(
                        "!",	"*",	"'",	"(",	")",	";",	"@",	"&",	"=",	"+",	"$",	",",	"/",	"?",	"#",	"[",	"]",
                        (shouldThreatDotAsReserved) ? "." : null
                    ).zeroOrMore()
                  ).oneOrMore()
                )
              );
              mappedGroups.put(groupName, paramName);
            } else {
              regex.append(
                RegexBuilder.create()
                  .append(";").quote(paramName).append("=")
                  .namedGroup(
                    groupName,
                    RegexBuilder.create().notCharactersClass(
                      "!",	"*",	"'",	"(",	")",	";",	"@",	"&",	"=",	"+",	"$",	"/",	"?",	"#",	"[",	"]",
                      (shouldThreatDotAsReserved) ? "." : null
                    ).zeroOrMore()
                  ).zeroOrOne()
              );
              mappedGroups.put(groupName, paramName);
            }
          }
        } else {
          throw RouterFactoryException.createSpecInvalidException("Missing parameter description for parameter name: " + paramName);
        }
        i++;
      }
      String toAppendQuoted = oasPath.substring(lastMatchEnd, (endSlash) ? oasPath.length() - 1 : oasPath.length());
      if (toAppendQuoted.length() != 0)
        regex.append(Pattern.quote(toAppendQuoted));
      if (endSlash)
        regex.append("\\/");
      return Optional.of(Pattern.compile(regex.toString()));
    } else {
      return Optional.empty();
    }
  }

  public Pattern getResolvedPattern() {
    return resolvedPattern;
  }

  public Map<String, String> getMappedGroups() {
    return mappedGroups;
  }

  private String solveParamStyle(Parameter parameter) {
    return (parameter.getStyle() != null) ? parameter.getStyle().toString() : "simple";
  }

  private boolean solveParamExplode(Parameter parameter) {
    return (parameter.getExplode() != null) ? parameter.getExplode() : false;
  }

  private boolean hasParameterWithStyle(String style) {
    return parameters.stream().map(this::solveParamStyle).anyMatch(s -> s.equals(style));
  }

  private boolean hasParameterWithStyleAndExplode(String style, boolean explode) {
    return parameters.stream().anyMatch(p -> solveParamStyle(p).equals(style) && solveParamExplode(p) == explode);
  }
}
