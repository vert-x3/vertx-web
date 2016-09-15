package io.vertx.ext.web;

import java.util.List;

import io.vertx.ext.web.impl.ParsableMIMEValue;

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
public interface ParsedHeaderValues {
  List<MIMEHeader> accept();
  List<ParsedHeaderValue> acceptCharset();
  List<ParsedHeaderValue> acceptEncoding();
  List<LanguageHeader> acceptLanguage();
  
  MIMEHeader contentType();
}
