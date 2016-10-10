require 'vertx-web/routing_context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Route
module VertxWeb
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
    # @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] arg0 
    # @return [self]
    def method(arg0=nil)
      if arg0.class == Symbol && !block_given?
        @j_del.java_method(:method, [Java::IoVertxCoreHttp::HttpMethod.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(arg0))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling method(arg0)"
    end
    # @param [String] arg0 
    # @return [self]
    def path(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:path, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling path(arg0)"
    end
    # @param [String] arg0 
    # @return [self]
    def path_regex(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:pathRegex, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling path_regex(arg0)"
    end
    # @param [String] arg0 
    # @return [self]
    def produces(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:produces, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling produces(arg0)"
    end
    # @param [String] arg0 
    # @return [self]
    def consumes(arg0=nil)
      if arg0.class == String && !block_given?
        @j_del.java_method(:consumes, [Java::java.lang.String.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling consumes(arg0)"
    end
    # @param [Fixnum] arg0 
    # @return [self]
    def order(arg0=nil)
      if arg0.class == Fixnum && !block_given?
        @j_del.java_method(:order, [Java::int.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling order(arg0)"
    end
    # @return [self]
    def last
      if !block_given?
        @j_del.java_method(:last, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling last()"
    end
    # @yield 
    # @return [self]
    def handler
      if block_given?
        @j_del.java_method(:handler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling handler()"
    end
    # @param [Proc] arg0 
    # @param [true,false] arg1 
    # @return [self]
    def blocking_handler(arg0=nil,arg1=nil)
      if block_given? && arg0 == nil && arg1 == nil
        @j_del.java_method(:blockingHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      elsif arg0.class == Proc && (arg1.class == TrueClass || arg1.class == FalseClass) && !block_given?
        @j_del.java_method(:blockingHandler, [Java::IoVertxCore::Handler.java_class,Java::boolean.java_class]).call((Proc.new { |event| arg0.call(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }),arg1)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling blocking_handler(arg0,arg1)"
    end
    # @yield 
    # @return [self]
    def failure_handler
      if block_given?
        @j_del.java_method(:failureHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::VertxWeb::RoutingContext)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling failure_handler()"
    end
    # @return [self]
    def remove
      if !block_given?
        @j_del.java_method(:remove, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling remove()"
    end
    # @return [self]
    def disable
      if !block_given?
        @j_del.java_method(:disable, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling disable()"
    end
    # @return [self]
    def enable
      if !block_given?
        @j_del.java_method(:enable, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling enable()"
    end
    # @param [true,false] arg0 
    # @return [self]
    def use_normalised_path(arg0=nil)
      if (arg0.class == TrueClass || arg0.class == FalseClass) && !block_given?
        @j_del.java_method(:useNormalisedPath, [Java::boolean.java_class]).call(arg0)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling use_normalised_path(arg0)"
    end
    # @return [String]
    def get_path
      if !block_given?
        return @j_del.java_method(:getPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_path()"
    end
  end
end
