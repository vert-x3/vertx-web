require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.CorsHandler
module VertxWeb
  #  A handler which implements server side http://www.w3.org/TR/cors/[CORS] support for Vert.x-Web.
  class CorsHandler
    # @private
    # @param j_del [::VertxWeb::CorsHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::CorsHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(arg0)"
    end
    #  Create a CORS handler
    # @param [String] allowedOriginPattern the allowed origin pattern
    # @return [::VertxWeb::CorsHandler] the handler
    def self.create(allowedOriginPattern=nil)
      if allowedOriginPattern.class == String && !block_given?
        return ::VertxWeb::CorsHandler.new(Java::IoVertxExtWebHandler::CorsHandler.java_method(:create, [Java::java.lang.String.java_class]).call(allowedOriginPattern))
      end
      raise ArgumentError, "Invalid arguments when calling create(allowedOriginPattern)"
    end
    #  Add an allowed method
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH] method the method to add
    # @return [self]
    def allowed_method(method=nil)
      if method.class == Symbol && !block_given?
        @j_del.java_method(:allowedMethod, [Java::IoVertxCoreHttp::HttpMethod.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(method))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling allowed_method(method)"
    end
    #  Add an allowed header
    # @param [String] headerName the allowed header name
    # @return [self]
    def allowed_header(headerName=nil)
      if headerName.class == String && !block_given?
        @j_del.java_method(:allowedHeader, [Java::java.lang.String.java_class]).call(headerName)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling allowed_header(headerName)"
    end
    #  Add a set of allowed headers
    # @param [Set<String>] headerNames the allowed header names
    # @return [self]
    def allowed_headers(headerNames=nil)
      if headerNames.class == Set && !block_given?
        @j_del.java_method(:allowedHeaders, [Java::JavaUtil::Set.java_class]).call(Java::JavaUtil::LinkedHashSet.new(headerNames.map { |element| element }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling allowed_headers(headerNames)"
    end
    #  Add an exposed header
    # @param [String] headerName the exposed header name
    # @return [self]
    def exposed_header(headerName=nil)
      if headerName.class == String && !block_given?
        @j_del.java_method(:exposedHeader, [Java::java.lang.String.java_class]).call(headerName)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling exposed_header(headerName)"
    end
    #  Add a set of exposed headers
    # @param [Set<String>] headerNames the exposed header names
    # @return [self]
    def exposed_headers(headerNames=nil)
      if headerNames.class == Set && !block_given?
        @j_del.java_method(:exposedHeaders, [Java::JavaUtil::Set.java_class]).call(Java::JavaUtil::LinkedHashSet.new(headerNames.map { |element| element }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling exposed_headers(headerNames)"
    end
    #  Set whether credentials are allowed
    # @param [true,false] allow true if allowed
    # @return [self]
    def allow_credentials(allow=nil)
      if (allow.class == TrueClass || allow.class == FalseClass) && !block_given?
        @j_del.java_method(:allowCredentials, [Java::boolean.java_class]).call(allow)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling allow_credentials(allow)"
    end
    #  Set how long the browser should cache the information
    # @param [Fixnum] maxAgeSeconds max age in seconds
    # @return [self]
    def max_age_seconds(maxAgeSeconds=nil)
      if maxAgeSeconds.class == Fixnum && !block_given?
        @j_del.java_method(:maxAgeSeconds, [Java::int.java_class]).call(maxAgeSeconds)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling max_age_seconds(maxAgeSeconds)"
    end
  end
end
