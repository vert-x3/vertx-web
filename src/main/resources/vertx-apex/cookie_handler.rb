require 'vertx-apex/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.CookieHandler
module VertxApex
  #  A handler which decodes cookies from the request, makes them available in the {::VertxApex::RoutingContext}
  #  and writes them back in the response.
  class CookieHandler
    # @private
    # @param j_del [::VertxApex::CookieHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::CookieHandler] the underlying java delegate
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
    #  Create a cookie handler
    # @return [::VertxApex::CookieHandler] the cookie handler
    def self.create
      if !block_given?
        return ::VertxApex::CookieHandler.new(Java::IoVertxExtApexHandler::CookieHandler.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
  end
end
