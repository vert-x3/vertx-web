require 'vertx-apex/routing_context'
require 'vertx-apex/router'
require 'vertx-apex/sock_js_socket'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.handler.sockjs.SockJSHandler
module VertxApex
  # 
  #  A handler that allows you to handle SockJS connections from clients.
  #  <p>
  #  We currently support version 0.3.3 of the SockJS protocol, which can be found in
  #  <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">this tag:</a>
  class SockJSHandler
    # @private
    # @param j_del [::VertxApex::SockJSHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::SockJSHandler] the underlying java delegate
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
    #  Create a SockJS handler
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [Hash] options options to configure the handler
    # @return [::VertxApex::SockJSHandler] the handler
    def self.create(vertx=nil,options=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && options == nil
        return ::VertxApex::SockJSHandler.new(Java::IoVertxExtApexHandlerSockjs::SockJSHandler.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del))
      elsif vertx.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::VertxApex::SockJSHandler.new(Java::IoVertxExtApexHandlerSockjs::SockJSHandler.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxExtApexHandlerSockjs::SockJSHandlerOptions.java_class]).call(vertx.j_del,Java::IoVertxExtApexHandlerSockjs::SockJSHandlerOptions.new(::Vertx::Util::Utils.to_json_object(options))))
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,options)"
    end
    #  Install SockJS test applications on a router - used when running the SockJS test suite
    # @param [::VertxApex::Router] router the router to install on
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @return [void]
    def self.install_test_applications(router=nil,vertx=nil)
      if router.class.method_defined?(:j_del) && vertx.class.method_defined?(:j_del) && !block_given?
        return Java::IoVertxExtApexHandlerSockjs::SockJSHandler.java_method(:installTestApplications, [Java::IoVertxExtApex::Router.java_class,Java::IoVertxCore::Vertx.java_class]).call(router.j_del,vertx.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling install_test_applications(router,vertx)"
    end
    #  Set a SockJS socket handler. This handler will be called with a SockJS socket whenever a SockJS connection
    #  is made from a client
    # @yield the handler
    # @return [self]
    def socket_handler
      if block_given?
        @j_del.java_method(:socketHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::VertxApex::SockJSSocket.new(event)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling socket_handler()"
    end
    #  Bridge the SockJS handler to the Vert.x event bus. This basically installs a built-in SockJS socket handler
    #  which takes SockJS traffic and bridges it to the event bus, thus allowing you to extend the server-side
    #  Vert.x event bus to browsers
    # @param [Hash] bridgeOptions options to configure the bridge with
    # @return [self]
    def bridge(bridgeOptions=nil)
      if bridgeOptions.class == Hash && !block_given?
        @j_del.java_method(:bridge, [Java::IoVertxExtApexHandlerSockjs::BridgeOptions.java_class]).call(Java::IoVertxExtApexHandlerSockjs::BridgeOptions.new(::Vertx::Util::Utils.to_json_object(bridgeOptions)))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling bridge(bridgeOptions)"
    end
  end
end
