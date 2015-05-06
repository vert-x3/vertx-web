require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.TimeoutHandler
module VertxApex
  #  Handler that will timeout requests if the response has not been written after a certain time.
  #  Timeout requests will be ended with an HTTP status code `408`.
  class TimeoutHandler
    # @private
    # @param j_del [::VertxApex::TimeoutHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::TimeoutHandler] the underlying java delegate
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
    #  Create a handler
    # @param [Fixnum] timeout the timeout, in ms
    # @return [::VertxApex::TimeoutHandler] the handler
    def self.create(timeout=nil)
      if !block_given? && timeout == nil
        return ::VertxApex::TimeoutHandler.new(Java::IoVertxExtApexHandler::TimeoutHandler.java_method(:create, []).call())
      elsif timeout.class == Fixnum && !block_given?
        return ::VertxApex::TimeoutHandler.new(Java::IoVertxExtApexHandler::TimeoutHandler.java_method(:create, [Java::long.java_class]).call(timeout))
      end
      raise ArgumentError, "Invalid arguments when calling create(timeout)"
    end
  end
end
