require 'vertx/buffer'
require 'vertx/multi_map'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.client.HttpResponse
module VertxWebClient
  #  An HTTP response.
  #  <p>
  #  The usual HTTP response attributes are available:
  #  <ul>
  #    <li>{::VertxWebClient::HttpResponse#status_code} the HTTP status code</li>
  #    <li>{::VertxWebClient::HttpResponse#status_message} the HTTP status message</li>
  #    <li>{::VertxWebClient::HttpResponse#headers} the HTTP headers</li>
  #    <li>{::VertxWebClient::HttpResponse#version} the HTTP version</li>
  #  </ul>
  #  <p>
  #  The body of the response is returned by {::VertxWebClient::HttpResponse#body} decoded as the format specified by the  that
  #  built the response.
  #  <p>
  #  Keep in mind that using this <code>HttpResponse</code> impose to fully buffer the response body and should be used for payload
  #  that can fit in memory.
  class HttpResponse
    # @private
    # @param j_del [::VertxWebClient::HttpResponse] the java delegate
    def initialize(j_del, j_arg_T=nil)
      @j_del = j_del
      @j_arg_T = j_arg_T != nil ? j_arg_T : ::Vertx::Util::unknown_type
    end
    # @private
    # @return [::VertxWebClient::HttpResponse] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [:HTTP_1_0,:HTTP_1_1,:HTTP_2] the version of the response
    def version
      if !block_given?
        if @cached_version != nil
          return @cached_version
        end
        return @cached_version = @j_del.java_method(:version, []).call().name.intern
      end
      raise ArgumentError, "Invalid arguments when calling version()"
    end
    # @return [Fixnum] the status code of the response
    def status_code
      if !block_given?
        if @cached_status_code != nil
          return @cached_status_code
        end
        return @cached_status_code = @j_del.java_method(:statusCode, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling status_code()"
    end
    # @return [String] the status message of the response
    def status_message
      if !block_given?
        if @cached_status_message != nil
          return @cached_status_message
        end
        return @cached_status_message = @j_del.java_method(:statusMessage, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling status_message()"
    end
    # @return [::Vertx::MultiMap] the headers
    def headers
      if !block_given?
        if @cached_headers != nil
          return @cached_headers
        end
        return @cached_headers = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:headers, []).call(),::Vertx::MultiMap)
      end
      raise ArgumentError, "Invalid arguments when calling headers()"
    end
    #  Return the first header value with the specified name
    # @param [String] headerName the header name
    # @return [String] the header value
    def get_header(headerName=nil)
      if headerName.class == String && !block_given?
        return @j_del.java_method(:getHeader, [Java::java.lang.String.java_class]).call(headerName)
      end
      raise ArgumentError, "Invalid arguments when calling get_header(#{headerName})"
    end
    # @return [::Vertx::MultiMap] the trailers
    def trailers
      if !block_given?
        if @cached_trailers != nil
          return @cached_trailers
        end
        return @cached_trailers = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:trailers, []).call(),::Vertx::MultiMap)
      end
      raise ArgumentError, "Invalid arguments when calling trailers()"
    end
    #  Return the first trailer value with the specified name
    # @param [String] trailerName the trailer name
    # @return [String] the trailer value
    def get_trailer(trailerName=nil)
      if trailerName.class == String && !block_given?
        return @j_del.java_method(:getTrailer, [Java::java.lang.String.java_class]).call(trailerName)
      end
      raise ArgumentError, "Invalid arguments when calling get_trailer(#{trailerName})"
    end
    # @return [Array<String>] the Set-Cookie headers (including trailers)
    def cookies
      if !block_given?
        if @cached_cookies != nil
          return @cached_cookies
        end
        return @cached_cookies = @j_del.java_method(:cookies, []).call().to_a.map { |elt| elt }
      end
      raise ArgumentError, "Invalid arguments when calling cookies()"
    end
    # @return [Object] the response body in the format it was decoded.
    def body
      if !block_given?
        if @cached_body != nil
          return @cached_body
        end
        return @cached_body = @j_arg_T.wrap(@j_del.java_method(:body, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling body()"
    end
    # @return [::Vertx::Buffer] the response body decoded as a 
    def body_as_buffer
      if !block_given?
        if @cached_body_as_buffer != nil
          return @cached_body_as_buffer
        end
        return @cached_body_as_buffer = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:bodyAsBuffer, []).call(),::Vertx::Buffer)
      end
      raise ArgumentError, "Invalid arguments when calling body_as_buffer()"
    end
    # @param [String] encoding 
    # @return [String] the response body decoded as a <code>String</code> given a specific <code>encoding</code>
    def body_as_string(encoding=nil)
      if !block_given? && encoding == nil
        if @cached_body_as_string != nil
          return @cached_body_as_string
        end
        return @cached_body_as_string = @j_del.java_method(:bodyAsString, []).call()
      elsif encoding.class == String && !block_given?
        return @j_del.java_method(:bodyAsString, [Java::java.lang.String.java_class]).call(encoding)
      end
      raise ArgumentError, "Invalid arguments when calling body_as_string(#{encoding})"
    end
    # @return [Hash{String => Object}] the response body decoded as a json object
    def body_as_json_object
      if !block_given?
        if @cached_body_as_json_object != nil
          return @cached_body_as_json_object
        end
        return @cached_body_as_json_object = @j_del.java_method(:bodyAsJsonObject, []).call() != nil ? JSON.parse(@j_del.java_method(:bodyAsJsonObject, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling body_as_json_object()"
    end
    # @param [Nil] type 
    # @return [Object] the response body decoded as the specified <code>type</code> with the Jackson mapper.
    def body_as_json(type=nil)
      if type.class == Class && !block_given?
        return ::Vertx::Util::Utils.v_type_of(type).wrap(@j_del.java_method(:bodyAsJson, [Java::JavaLang::Class.java_class]).call(::Vertx::Util::Utils.j_class_of(type)))
      end
      raise ArgumentError, "Invalid arguments when calling body_as_json(#{type})"
    end
  end
end
