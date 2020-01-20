package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.OpenAPIHolder;
import io.vertx.ext.web.openapi.RouterFactoryException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3PathResolver {

  final String oasPath;
  final List<JsonObject> parameters;
  final OpenAPIHolder openAPIHolder;

  Pattern resolvedPattern;
  //Key is new name, value is old name
  Map<String, String> mappedGroups;

  public static final Pattern OAS_PATH_PARAMETERS_PATTERN = Pattern.compile("\\{{1}[.;?*+]*([^\\{\\}.;?*+]+)[^\\}]*\\}{1}");
  public static final Pattern ILLEGAL_PATH_MATCHER = Pattern.compile("\\{[^\\/]*\\/[^\\/]*\\}");

  private boolean shouldThreatDotAsReserved;

  public OpenAPI3PathResolver(String oasPath, List<JsonObject> parameters, OpenAPIHolder openAPIHolder) {
    this.oasPath = oasPath;

    // Filter parameters to get only path parameters
    if (parameters != null)
      this.parameters = parameters.stream().filter(parameter -> parameter.getString("in").equals("path")).collect(Collectors.toList());
    else
      this.parameters = new ArrayList<>();

    this.openAPIHolder = openAPIHolder;

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
      throw RouterFactoryException.createUnsupportedSpecFeature("Path template not supported");

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

        //TODO HO CAPITO IL PROBLEMA MA NON HO VOGLIA DI RISOLVERLO ORA
        // PERMUTAZIONE DEGLI ARGOMENTI FOTTE TUTTO

        String paramName = parametersMatcher.group(1);
        Optional<JsonObject> parameterOptional = parameters.stream().filter(p -> p.getString("name").equals(paramName)).findFirst();
        if (parameterOptional.isPresent()) {
          // For every parameter style I have to generate a different regular expression
          JsonObject parameter = parameterOptional.get();

          JsonObject fakeSchema = OpenApi3Utils.generateFakeSchema(parameter.getJsonObject("schema", new JsonObject()), openAPIHolder);
          String style = parameter.getString("style", "simple");
          boolean explode = parameter.getBoolean("explode", false);
          boolean isObject = OpenApi3Utils.isSchemaObjectOrCombinators(fakeSchema);
          boolean isArray = OpenApi3Utils.isSchemaArray(fakeSchema);

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
              Map<String, JsonObject> properties = OpenApi3Utils.solveObjectParameters(fakeSchema);
              List<RegexBuilder> regexBuilders = new ArrayList<>();
              for (Map.Entry<String, JsonObject> entry : properties.entrySet()) {
                groupName = "p" + i;
                regexBuilders.add(
                  RegexBuilder.create().group(
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
              regex.append(
                RegexBuilder.create().anyOfGroup(
                  properties.size(),
                  regexBuilders.stream()
                )
              );
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
              Map<String, JsonObject> properties = OpenApi3Utils.solveObjectParameters(fakeSchema);
              List<RegexBuilder> regexBuilders = new ArrayList<>();
              for (Map.Entry<String, JsonObject> entry : properties.entrySet()) {
                groupName = "p" + i;
                regexBuilders.add(
                  RegexBuilder.create().group(
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
              regex.append(
                RegexBuilder.create().anyOfGroup(
                  properties.size(),
                  regexBuilders.stream()
                )
              );
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
          throw RouterFactoryException.createUnsupportedSpecFeature("Missing parameter definition for parameter name: " + paramName);
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

  private boolean hasParameterWithStyle(String style) {
    return parameters.stream().map(p -> p.getString("style", "simple")).anyMatch(s -> s.equals(style));
  }

  private boolean hasParameterWithStyleAndExplode(String style, boolean explode) {
    return parameters.stream().anyMatch(p -> p.getString("style", "simple").equals(style) && p.getBoolean("explode", false) == explode);
  }
}
