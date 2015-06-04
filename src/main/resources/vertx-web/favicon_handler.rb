require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.FaviconHandler
module VertxWeb
  #  A handler that serves favicons.
  #  <p>
  #  If no file system path is specified it will attempt to serve a resource called `favicon.ico` from the classpath.
  class FaviconHandler
    # @private
    # @param j_del [::VertxWeb::FaviconHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::FaviconHandler] the underlying java delegate
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
    #  Create a handler attempting to load favicon file from the specified path, and with the specified max cache time
    # @overload create()
    # @overload create(path)
    #   @param [String] path the path
    # @overload create(maxAgeSeconds)
    #   @param [Fixnum] maxAgeSeconds max how long the file will be cached by browser, in seconds
    # @overload create(path,maxAgeSeconds)
    #   @param [String] path the path
    #   @param [Fixnum] maxAgeSeconds max how long the file will be cached by browser, in seconds
    # @return [::VertxWeb::FaviconHandler] the handler
    def self.create(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::VertxWeb::FaviconHandler.new(Java::IoVertxExtWebHandler::FaviconHandler.java_method(:create, []).call())
      elsif param_1.class == String && !block_given? && param_2 == nil
        return ::VertxWeb::FaviconHandler.new(Java::IoVertxExtWebHandler::FaviconHandler.java_method(:create, [Java::java.lang.String.java_class]).call(param_1))
      elsif param_1.class == Fixnum && !block_given? && param_2 == nil
        return ::VertxWeb::FaviconHandler.new(Java::IoVertxExtWebHandler::FaviconHandler.java_method(:create, [Java::long.java_class]).call(param_1))
      elsif param_1.class == String && param_2.class == Fixnum && !block_given?
        return ::VertxWeb::FaviconHandler.new(Java::IoVertxExtWebHandler::FaviconHandler.java_method(:create, [Java::java.lang.String.java_class,Java::long.java_class]).call(param_1,param_2))
      end
      raise ArgumentError, "Invalid arguments when calling create(param_1,param_2)"
    end
  end
end
