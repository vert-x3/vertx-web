require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Route
module VertxWeb
  #  A route is a holder for a set of criteria which determine whether an HTTP request or failure should be routed
  #  to a handler.
  class Route
    # @private
    # @param j_del [::VertxWeb::Route] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Route] the underlying java delegate
    def j_del
      @j_del
    end
    #  Add an HTTP method for this route. By default a route will match all HTTP methods. If any are specified then the route
    #  will only match any of the specified methods
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the HTTP method to add
    # @return [self]
    def method(method=nil)
      if method.class == Symbol && !block_given?
        @j_del.java_method(:method, [Java::IoVertxCoreHttp::HttpMethod.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(method))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling method(method)"
    end
    #  Set the path prefix for this route. If set then this route will only match request URI paths which start with this
    #  path prefix. Only a single path or path regex can be set for a route.
    # @param [String] path the path prefix
    # @return [self]
    def path(path=nil)
      if path.class == String && !block_given?
        @j_del.java_method(:path, [Java::java.lang.String.java_class]).call(path)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling path(path)"
    end
    #  Set the path prefix as a regular expression. If set then this route will only match request URI paths, the beginning
    #  of which match the regex. Only a single path or path regex can be set for a route.
    # @param [String] path the path regex
    # @return [self]
    def path_regex(path=nil)
      if path.class == String && !block_given?
        @j_del.java_method(:pathRegex, [Java::java.lang.String.java_class]).call(path)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling path_regex(path)"
    end
    #  Add a content type produced by this route. Used for content based routing.
    # @param [String] contentType the content type
    # @return [self]
    def produces(contentType=nil)
      if contentType.class == String && !block_given?
        @j_del.java_method(:produces, [Java::java.lang.String.java_class]).call(contentType)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling produces(contentType)"
    end
    #  Add a content type consumed by this route. Used for content based routing.
    # @param [String] contentType the content type
    # @return [self]
    def consumes(contentType=nil)
      if contentType.class == String && !block_given?
        @j_del.java_method(:consumes, [Java::java.lang.String.java_class]).call(contentType)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling consumes(contentType)"
    end
    #  Specify the order for this route. The router tests routes in that order.
    # @param [Fixnum] order the order
    # @return [self]
    def order(order=nil)
      if order.class == Fixnum && !block_given?
        @j_del.java_method(:order, [Java::int.java_class]).call(order)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling order(order)"
    end
    #  Specify this is the last route for the router.
    # @return [self]
    def last
      if !block_given?
        @j_del.java_method(:last, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling last()"
    end
    #  Specify a request handler for the route. The router routes requests to handlers depending on whether the various
    #  criteria such as method, path, etc match. There can be only one request handler for a route. If you set this more
    #  than once it will overwrite the previous handler.
    # @yield the request handler
    # @return [self]
    def handler
      if block_given?
        @j_del.java_method(:handler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling handler()"
    end
    #  Specify a blocking request handler for the route.
    #  This method works just like {::VertxWeb::Route#handler} excepted that it will run the blocking handler on a worker thread
    #  so that it won't block the event loop. Note that it's safe to call context.next() from the
    #  blocking handler as it will be executed on the event loop context (and not on the worker thread.
    # 
    #  If the blocking handler is ordered it means that any blocking handlers for the same context are never executed
    #  concurrently but always in the order they were called. The default value of ordered is true. If you do not want this
    #  behaviour and don't mind if your blocking handlers are executed in parallel you can set ordered to false.
    # @param [Proc] requestHandler the blocking request handler
    # @param [true,false] ordered if true handlers are executed in sequence, otherwise are run in parallel
    # @return [self]
    def blocking_handler(requestHandler=nil,ordered=nil)
      if block_given? && requestHandler == nil && ordered == nil
        @j_del.java_method(:blockingHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      elsif requestHandler.class == Proc && (ordered.class == TrueClass || ordered.class == FalseClass) && !block_given?
        @j_del.java_method(:blockingHandler, [Java::IoVertxCore::Handler.java_class,Java::boolean.java_class]).call((Proc.new { |event| requestHandler.call(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }),ordered)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling blocking_handler(requestHandler,ordered)"
    end
    #  Specify a failure handler for the route. The router routes failures to failurehandlers depending on whether the various
    #  criteria such as method, path, etc match. There can be only one failure handler for a route. If you set this more
    #  than once it will overwrite the previous handler.
    # @yield the request handler
    # @return [self]
    def failure_handler
      if block_given?
        @j_del.java_method(:failureHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling failure_handler()"
    end
    #  Remove this route from the router
    # @return [self]
    def remove
      if !block_given?
        @j_del.java_method(:remove, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling remove()"
    end
    #  Disable this route. While disabled the router will not route any requests or failures to it.
    # @return [self]
    def disable
      if !block_given?
        @j_del.java_method(:disable, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling disable()"
    end
    #  Enable this route.
    # @return [self]
    def enable
      if !block_given?
        @j_del.java_method(:enable, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling enable()"
    end
    # @yield 
    # @return [::VertxWeb::Route]
    def then
      if block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:then, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) })),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling then()"
    end
    #  If true then the normalised request path will be used when routing (e.g. removing duplicate /)
    #  Default is true
    # @param [true,false] useNormalisedPath use normalised path for routing?
    # @return [self]
    def use_normalised_path(useNormalisedPath=nil)
      if (useNormalisedPath.class == TrueClass || useNormalisedPath.class == FalseClass) && !block_given?
        @j_del.java_method(:useNormalisedPath, [Java::boolean.java_class]).call(useNormalisedPath)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling use_normalised_path(useNormalisedPath)"
    end
    # @return [String] the path prefix (if any) for this route
    def get_path
      if !block_given?
        return @j_del.java_method(:getPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_path()"
    end
  end
end
