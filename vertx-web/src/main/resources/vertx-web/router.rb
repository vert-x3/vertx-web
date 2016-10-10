require 'vertx-web/route'
require 'vertx/http_server_request'
require 'vertx/vertx'
require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Router
module VertxWeb
  class Router
    # @private
    # @param j_del [::VertxWeb::Router] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Router] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::Vertx::Vertx] vertx 
    # @return [::VertxWeb::Router]
    def self.router(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Router.java_method(:router, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::Router)
      end
      raise ArgumentError, "Invalid arguments when calling router(vertx)"
    end
    # @param [::Vertx::HttpServerRequest] arg0 
    # @return [void]
    def accept(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:accept, [Java::IoVertxCoreHttp::HttpServerRequest.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling accept(arg0)"
    end
    # @overload route()
    # @overload route(arg0)
    #   @param [String] arg0 
    # @overload route(arg0,arg1)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] arg0 
    #   @param [String] arg1 
    # @return [::VertxWeb::Route]
    def route(param_1=nil,param_2=nil)
      if !block_given? && param_1 == nil && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:route, []).call(),::VertxWeb::Route)
      elsif param_1.class == String && !block_given? && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:route, [Java::java.lang.String.java_class]).call(param_1),::VertxWeb::Route)
      elsif param_1.class == Symbol && param_2.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:route, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1),param_2),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling route(param_1,param_2)"
    end
    # @overload routeWithRegex(arg0)
    #   @param [String] arg0 
    # @overload routeWithRegex(arg0,arg1)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] arg0 
    #   @param [String] arg1 
    # @return [::VertxWeb::Route]
    def route_with_regex(param_1=nil,param_2=nil)
      if param_1.class == String && !block_given? && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:routeWithRegex, [Java::java.lang.String.java_class]).call(param_1),::VertxWeb::Route)
      elsif param_1.class == Symbol && param_2.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:routeWithRegex, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1),param_2),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling route_with_regex(param_1,param_2)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def get(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling get(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def get_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling get_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def head(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:head, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling head(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def head_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:headWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling head_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def options(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:options, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:options, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling options(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def options_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:optionsWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling options_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def put(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:put, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling put(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def put_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:putWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling put_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def post(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:post, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling post(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def post_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:postWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling post_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def delete(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:delete, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling delete(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def delete_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:deleteWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling delete_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def trace(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:trace, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:trace, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling trace(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def trace_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:traceWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling trace_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def connect(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:connect, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:connect, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling connect(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def connect_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:connectWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling connect_with_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def patch(arg0=nil)
      if !block_given? && arg0 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, []).call(),::VertxWeb::Route)
      elsif arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patch, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling patch(arg0)"
    end
    # @param [String] arg0 
    # @return [::VertxWeb::Route]
    def patch_with_regex(arg0=nil)
      if arg0.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:patchWithRegex, [Java::java.lang.String.java_class]).call(arg0),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling patch_with_regex(arg0)"
    end
    # @return [Array<::VertxWeb::Route>]
    def get_routes
      if !block_given?
        return @j_del.java_method(:getRoutes, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::Route) }
      end
      raise ArgumentError, "Invalid arguments when calling get_routes()"
    end
    # @return [self]
    def clear
      if !block_given?
        @j_del.java_method(:clear, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling clear()"
    end
    # @param [String] arg0 
    # @param [::VertxWeb::Router] arg1 
    # @return [self]
    def mount_sub_router(arg0=nil,arg1=nil)
      if arg0.class == String && arg1.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:mountSubRouter, [Java::java.lang.String.java_class,Java::IoVertxExtWeb::Router.java_class]).call(arg0,arg1.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling mount_sub_router(arg0,arg1)"
    end
    # @yield 
    # @return [self]
    def exception_handler
      if block_given?
        @j_del.java_method(:exceptionHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.from_throwable(event)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling exception_handler()"
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle_context(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handleContext, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle_context(arg0)"
    end
    # @param [::VertxWeb::RoutingContext] arg0 
    # @return [void]
    def handle_failure(arg0=nil)
      if arg0.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:handleFailure, [Java::IoVertxExtWeb::RoutingContext.java_class]).call(arg0.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling handle_failure(arg0)"
    end
  end
end
