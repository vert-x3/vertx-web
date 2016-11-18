require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.VirtualHostHandler
module VertxWeb
  #  Handler that will filter requests based on the request Host name.
  class VirtualHostHandler
    # @private
    # @param j_del [::VertxWeb::VirtualHostHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::VirtualHostHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == VirtualHostHandler
    end
    def @@j_api_type.wrap(obj)
      VirtualHostHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::VirtualHostHandler.java_class
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
    # @param [String] hostname 
    # @yield 
    # @return [::VertxWeb::VirtualHostHandler] the handler
    def self.create(hostname=nil)
      if hostname.class == String && block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::VirtualHostHandler.java_method(:create, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(hostname,(Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) })),::VertxWeb::VirtualHostHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{hostname})"
    end
  end
end
