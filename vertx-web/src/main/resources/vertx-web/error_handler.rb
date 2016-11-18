require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.ErrorHandler
module VertxWeb
  #  A pretty error handler for rendering error pages.
  class ErrorHandler
    # @private
    # @param j_del [::VertxWeb::ErrorHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ErrorHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == ErrorHandler
    end
    def @@j_api_type.wrap(obj)
      ErrorHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::ErrorHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Create an error handler
    # @overload create()
    # @overload create(displayExceptionDetails)
    #   @param [true,false] displayExceptionDetails true if exception details should be displayed
    # @overload create(errorTemplateName)
    #   @param [String] errorTemplateName the error template name - will be looked up from the classpath
    # @overload create(errorTemplateName,displayExceptionDetails)
    #   @param [String] errorTemplateName the error template name - will be looked up from the classpath
    #   @param [true,false] displayExceptionDetails true if exception details should be displayed
    # @return [::VertxWeb::ErrorHandler] the handler
    def self.create(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ErrorHandler.java_method(:create, []).call(),::VertxWeb::ErrorHandler)
      elsif (param_1.class == TrueClass || param_1.class == FalseClass) && !block_given? && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ErrorHandler.java_method(:create, [Java::boolean.java_class]).call(param_1),::VertxWeb::ErrorHandler)
      elsif param_1.class == String && !block_given? && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ErrorHandler.java_method(:create, [Java::java.lang.String.java_class]).call(param_1),::VertxWeb::ErrorHandler)
      elsif param_1.class == String && (param_2.class == TrueClass || param_2.class == FalseClass) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ErrorHandler.java_method(:create, [Java::java.lang.String.java_class,Java::boolean.java_class]).call(param_1,param_2),::VertxWeb::ErrorHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{param_1},#{param_2})"
    end
  end
end
