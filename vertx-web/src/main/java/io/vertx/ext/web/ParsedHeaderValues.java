package io.vertx.ext.web;

import java.util.Collection;
import java.util.List;

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
  /**
   * @return List of MIME values in the {@code Accept} header
   */
  List<MIMEHeader> accept();
  /**
   * @return List of charset values in the {@code Accept-Charset} header
   */
  List<ParsedHeaderValue> acceptCharset();
  /**
   * @return List of encofing values in the {@code Accept-Encoding} header
   */
  List<ParsedHeaderValue> acceptEncoding();
  /**
   * @return List of languages in the {@code Accept-Language} header
   */
  List<LanguageHeader> acceptLanguage();

  /**
   * @return MIME value in the {@code Content-Type} header
   */
  MIMEHeader contentType();

  /**
   * Given the sorted list of parsed header values the user has sent and an Iterable of acceptable values:
   * It finds the first accepted header that matches any inside the Iterable.
   * <p>
   *  <b>Note:</b> This method is intended for internal usage.
   * </p>
   *
   * @param accepted The sorted list of headers to find the best one.
   * @param in The headers to match against.
   * @return The first header that matched, otherwise empty if none matched
   */
  @GenIgnore
  <T extends ParsedHeaderValue> T findBestUserAcceptedIn(List<T> accepted, Collection<T> in);
}
