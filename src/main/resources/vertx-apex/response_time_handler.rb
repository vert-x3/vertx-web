require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.ResponseTimeHandler
module VertxApex
  #  Handler which adds a header `x-response-time` in the response of matching requests containing the time taken
  #  in ms to process the request.
  class ResponseTimeHandler
    # @private
    # @param j_del [::VertxApex::ResponseTimeHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::ResponseTimeHandler] the underlying java delegate
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
    # @return [::VertxApex::ResponseTimeHandler] the handler
    def self.create
      if !block_given?
        return ::VertxApex::ResponseTimeHandler.new(Java::IoVertxExtApexHandler::ResponseTimeHandler.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
  end
end
