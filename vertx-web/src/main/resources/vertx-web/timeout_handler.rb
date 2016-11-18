require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.TimeoutHandler
module VertxWeb
  #  Handler that will timeout requests if the response has not been written after a certain time.
  #  Timeout requests will be ended with an HTTP status code `503`.
  class TimeoutHandler
    # @private
    # @param j_del [::VertxWeb::TimeoutHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::TimeoutHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == TimeoutHandler
    end
    def @@j_api_type.wrap(obj)
      TimeoutHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::TimeoutHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Create a handler
    # @param [Fixnum] timeout the timeout, in ms
    # @param [Fixnum] errorCode 
    # @return [::VertxWeb::TimeoutHandler] the handler
    def self.create(timeout=nil,errorCode=nil)
      if !block_given? && timeout == nil && errorCode == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::TimeoutHandler.java_method(:create, []).call(),::VertxWeb::TimeoutHandler)
      elsif timeout.class == Fixnum && !block_given? && errorCode == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::TimeoutHandler.java_method(:create, [Java::long.java_class]).call(timeout),::VertxWeb::TimeoutHandler)
      elsif timeout.class == Fixnum && errorCode.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::TimeoutHandler.java_method(:create, [Java::long.java_class,Java::int.java_class]).call(timeout,errorCode),::VertxWeb::TimeoutHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{timeout},#{errorCode})"
    end
  end
end
