require 'vertx-web/parsed_header_value'
require 'vertx-web/mime_header'
require 'vertx-web/language_header'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.ParsedHeaderValues
module VertxWeb
  #  A container with the request's headers that are meaningful enough to be parsed
  #  Contains:
  #  <ul>
  #  <li>Accept -> MIME header, parameters and sortable</li>
  #  <li>Accept-Charset -> Parameters and sortable</li>
  #  <li>Accept-Encoding -> Parameters and sortable</li>
  #  <li>Accept-Language -> Parameters and sortable</li>
  #  <li>Content-Type -> MIME header and parameters</li>
  #  </ul>
  # 
  class ParsedHeaderValues
    # @private
    # @param j_del [::VertxWeb::ParsedHeaderValues] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ParsedHeaderValues] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [Array<::VertxWeb::MIMEHeader>]
    def accept
      if !block_given?
        return @j_del.java_method(:accept, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::MIMEHeader) }
      end
      raise ArgumentError, "Invalid arguments when calling accept()"
    end
    # @return [Array<::VertxWeb::ParsedHeaderValue>]
    def accept_charset
      if !block_given?
        return @j_del.java_method(:acceptCharset, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::ParsedHeaderValueImpl) }
      end
      raise ArgumentError, "Invalid arguments when calling accept_charset()"
    end
    # @return [Array<::VertxWeb::ParsedHeaderValue>]
    def accept_encoding
      if !block_given?
        return @j_del.java_method(:acceptEncoding, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::ParsedHeaderValueImpl) }
      end
      raise ArgumentError, "Invalid arguments when calling accept_encoding()"
    end
    # @return [Array<::VertxWeb::LanguageHeader>]
    def accept_language
      if !block_given?
        return @j_del.java_method(:acceptLanguage, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::LanguageHeader) }
      end
      raise ArgumentError, "Invalid arguments when calling accept_language()"
    end
    # @return [::VertxWeb::MIMEHeader]
    def content_type
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:contentType, []).call(),::VertxWeb::MIMEHeader)
      end
      raise ArgumentError, "Invalid arguments when calling content_type()"
    end
  end
end
