package io.vertx.ext.web.openapi.impl;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegexBuilder {

  StringBuilder stringBuilder;

  RegexBuilder() {
    stringBuilder = new StringBuilder();
  }

  public RegexBuilder escapeCharacter(String c) {
    stringBuilder.append("\\" + c);
    return this;
  }

  public RegexBuilder escapeCharacters(String... chars) {
    for (String c : chars)
      stringBuilder.append("\\" + c);
    return this;
  }

  public RegexBuilder append(String s) {
    stringBuilder.append(s);
    return this;
  }

  public RegexBuilder quote(String s) {
    stringBuilder.append(Pattern.quote(s));
    return this;
  }

  public RegexBuilder group(RegexBuilder group) {
    stringBuilder.append("(");
    stringBuilder.append(group.toString());
    stringBuilder.append(")");
    return this;
  }


  public RegexBuilder atomicGroup(RegexBuilder group) {
    stringBuilder.append("(?>");
    stringBuilder.append(group.toString());
    stringBuilder.append(")");
    return this;
  }

  public RegexBuilder optionalAtomicGroup(RegexBuilder group) {
    stringBuilder.append("(?>");
    stringBuilder.append(group.toString());
    stringBuilder.append(")?");
    return this;
  }

  public RegexBuilder namedGroup(String name, RegexBuilder group) {
    stringBuilder.append("(?<");
    stringBuilder.append(name);
    stringBuilder.append(">");
    stringBuilder.append(group.toString());
    stringBuilder.append(")");
    return this;
  }

  public RegexBuilder anyOfGroup(int maxTimes, RegexBuilder... groups) {
    anyOfGroup(maxTimes, Arrays.stream(groups));
    return this;
  }

  public RegexBuilder anyOfGroup(int maxTimes, Stream<RegexBuilder> groups) {
    stringBuilder.append("(");
    stringBuilder.append(groups.map(RegexBuilder::toString).collect(Collectors.joining("|")));
    stringBuilder
      .append("){0,")
      .append(maxTimes)
      .append("}");
    return this;
  }

  public RegexBuilder negativeLookaheadGroup(RegexBuilder group) {
    stringBuilder.append("(!");
    stringBuilder.append(group.toString());
    stringBuilder.append(")");
    return this;
  }

  public RegexBuilder notCharactersClass(RegexBuilder group) {
    stringBuilder.append("[^");
    stringBuilder.append(group.toString());
    stringBuilder.append("]");
    return this;
  }

  public RegexBuilder notCharactersClass(String... chars) {
    stringBuilder.append("[^");
    for (String c : chars)
      if (c != null && c.length() != 0)
        stringBuilder.append("\\" + c);
    stringBuilder.append("]");
    return this;
  }

  public RegexBuilder charactersClass(RegexBuilder group) {
    stringBuilder.append("[");
    stringBuilder.append(group.toString());
    stringBuilder.append("]");
    return this;
  }

  public RegexBuilder charactersClass(String... chars) {
    stringBuilder.append("[");
    for (String c : chars)
      stringBuilder.append("\\" + c);
    stringBuilder.append("]");
    return this;
  }

  public RegexBuilder zeroOrOne() {
    stringBuilder.append("?");
    return this;
  }

  public RegexBuilder zeroOrMore() {
    stringBuilder.append("*");
    return this;
  }

  public RegexBuilder oneOrMore() {
    stringBuilder.append("+");
    return this;
  }

  static RegexBuilder create() {
    return new RegexBuilder();
  }

  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
