require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.LoggerHandler
module VertxWeb
  #  A handler which logs request information to the Vert.x logger.
  class LoggerHandler
    # @private
    # @param j_del [::VertxWeb::LoggerHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::LoggerHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == LoggerHandler
    end
    def @@j_api_type.wrap(obj)
      LoggerHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::LoggerHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Create a handler with he specified format
    # @overload create()
    # @overload create(format)
    #   @param [:DEFAULT,:SHORT,:TINY] format the format
    # @overload create(immediate,format)
    #   @param [true,false] immediate true if logging should occur as soon as request arrives
    #   @param [:DEFAULT,:SHORT,:TINY] format the format
    # @return [::VertxWeb::LoggerHandler] the handler
    def self.create(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::LoggerHandler.java_method(:create, []).call(),::VertxWeb::LoggerHandler)
      elsif param_1.class == Symbol && !block_given? && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::LoggerHandler.java_method(:create, [Java::IoVertxExtWebHandler::LoggerFormat.java_class]).call(Java::IoVertxExtWebHandler::LoggerFormat.valueOf(param_1)),::VertxWeb::LoggerHandler)
      elsif (param_1.class == TrueClass || param_1.class == FalseClass) && param_2.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::LoggerHandler.java_method(:create, [Java::boolean.java_class,Java::IoVertxExtWebHandler::LoggerFormat.java_class]).call(param_1,Java::IoVertxExtWebHandler::LoggerFormat.valueOf(param_2)),::VertxWeb::LoggerHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{param_1},#{param_2})"
    end
  end
end
