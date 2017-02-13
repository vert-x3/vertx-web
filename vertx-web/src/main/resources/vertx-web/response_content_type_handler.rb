require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.ResponseContentTypeHandler
module VertxWeb
  #  A handler which sets the response content type automatically according to the best <code>Accept</code> header match.
  # 
  #  The header is set only if:
  #  <ul>
  #  <li>no object is stored in the routing context under the name DEFAULT_DISABLE_FLAG</li>
  #  <li>a match is found</li>
  #  <li>the header is not present already</li>
  #  <li>content length header is absent or set to something different than zero</li>
  #  </ul>
  class ResponseContentTypeHandler
    # @private
    # @param j_del [::VertxWeb::ResponseContentTypeHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ResponseContentTypeHandler] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == ResponseContentTypeHandler
    end
    def @@j_api_type.wrap(obj)
      ResponseContentTypeHandler.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtWebHandler::ResponseContentTypeHandler.java_class
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handle, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle(#{arg0})"
    end
    #  Create a response content type handler with a custom disable flag.
    # @param [String] disableFlag 
    # @return [::VertxWeb::ResponseContentTypeHandler] the response content type handler
    def self.create(disableFlag=nil)
      if !block_given? && disableFlag == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ResponseContentTypeHandler.java_method(:create, []).call(),::VertxWeb::ResponseContentTypeHandler)
      elsif disableFlag.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebHandler::ResponseContentTypeHandler.java_method(:create, [Java::java.lang.String.java_class]).call(disableFlag),::VertxWeb::ResponseContentTypeHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{disableFlag})"
    end
  end
end
