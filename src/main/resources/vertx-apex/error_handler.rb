require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.ErrorHandler
module VertxApex
  #  A pretty error handler for rendering error pages.
  class ErrorHandler
    # @private
    # @param j_del [::VertxApex::ErrorHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::ErrorHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::VertxApex::RoutingContext] arg0
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtApex::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(arg0)"
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
    # @return [::VertxApex::ErrorHandler] the handler
    def self.create(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::VertxApex::ErrorHandler.new(Java::IoVertxExtApexHandler::ErrorHandler.java_method(:create, []).call())
      elsif (param_1.class == TrueClass || param_1.class == FalseClass) && !block_given? && param_2 == nil
        return ::VertxApex::ErrorHandler.new(Java::IoVertxExtApexHandler::ErrorHandler.java_method(:create, [Java::boolean.java_class]).call(param_1))
      elsif param_1.class == String && !block_given? && param_2 == nil
        return ::VertxApex::ErrorHandler.new(Java::IoVertxExtApexHandler::ErrorHandler.java_method(:create, [Java::java.lang.String.java_class]).call(param_1))
      elsif param_1.class == String && (param_2.class == TrueClass || param_2.class == FalseClass) && !block_given?
        return ::VertxApex::ErrorHandler.new(Java::IoVertxExtApexHandler::ErrorHandler.java_method(:create, [Java::java.lang.String.java_class,Java::boolean.java_class]).call(param_1,param_2))
      end
      raise ArgumentError, "Invalid arguments when calling create(param_1,param_2)"
    end
  end
end
