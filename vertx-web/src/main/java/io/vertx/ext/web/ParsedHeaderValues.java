package io.vertx.ext.web;

import java.util.List;
import java.util.Optional;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A container with the request's headers that are meaningful enough to be parsed
 * Contains:
 * <ul>
 * <li>Accept -> MIME header, parameters and sortable</li>
 * <li>Accept-Charset -> Parameters and sortable</li>
 * <li>Accept-Encoding -> Parameters and sortable</li>
 * <li>Accept-Language -> Parameters and sortable</li>
 * <li>Content-Type -> MIME header and parameters</li>
 * </ul>
 *
 */
@VertxGen
public interface ParsedHeaderValues {
  List<MIMEHeader> accept();
  List<ParsedHeaderValue> acceptCharset();
  List<ParsedHeaderValue> acceptEncoding();
  List<LanguageHeader> acceptLanguage();
  
  MIMEHeader contentType();
  
  /**
   * Given the sorted list of parsed header values the user has sent and an Iterable of acceptable values:
   * It finds the first accepted header that matches any inside the Iterable.
   * @param accepted The sorted list of headers to find the best one.
   * @param in The headers to match against.
   * @return The first header that matched, otherwise empty if none matched
   */
  @GenIgnore
  <T extends ParsedHeaderValue> Optional<T> findBestUserAcceptedIn(List<T> accepted, Iterable<T> in);
}
