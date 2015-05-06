require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.LoggerHandler
module VertxApex
  #  A handler which logs request information to the Vert.x logger.
  class LoggerHandler
    # @private
    # @param j_del [::VertxApex::LoggerHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::LoggerHandler] the underlying java delegate
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
    #  Create a handler with he specified format
    # @overload create()
    # @overload create(format)
    #   @param [:DEFAULT,:SHORT,:TINY] format the format
    # @overload create(immediate,format)
    #   @param [true,false] immediate true if logging should occur as soon as request arrives
    #   @param [:DEFAULT,:SHORT,:TINY] format the format
    # @return [::VertxApex::LoggerHandler] the handler
    def self.create(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::VertxApex::LoggerHandler.new(Java::IoVertxExtApexHandler::LoggerHandler.java_method(:create, []).call())
      elsif param_1.class == Symbol && !block_given? && param_2 == nil
        return ::VertxApex::LoggerHandler.new(Java::IoVertxExtApexHandler::LoggerHandler.java_method(:create, [Java::IoVertxExtApexHandlerLoggerHandler::Format.java_class]).call(Java::IoVertxExtApexHandlerLoggerHandler::Format.valueOf(param_1)))
      elsif (param_1.class == TrueClass || param_1.class == FalseClass) && param_2.class == Symbol && !block_given?
        return ::VertxApex::LoggerHandler.new(Java::IoVertxExtApexHandler::LoggerHandler.java_method(:create, [Java::boolean.java_class,Java::IoVertxExtApexHandlerLoggerHandler::Format.java_class]).call(param_1,Java::IoVertxExtApexHandlerLoggerHandler::Format.valueOf(param_2)))
      end
      raise ArgumentError, "Invalid arguments when calling create(param_1,param_2)"
    end
  end
end
