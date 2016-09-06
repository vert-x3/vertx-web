require 'vertx-web/sock_js_socket'
require 'vertx/future'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.sockjs.BridgeEvent
module VertxWeb
  #  Represents an event that occurs on the event bus bridge.
  #  <p>
  #  Please consult the documentation for a full explanation.
  class BridgeEvent < ::Vertx::Future
    # @private
    # @param j_del [::VertxWeb::BridgeEvent] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::BridgeEvent] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [true,false]
    def complete?
      if !block_given?
        return @j_del.java_method(:isComplete, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling complete?()"
    end
    # @yield 
    # @return [self]
    def set_handler
      if block_given?
        @j_del.java_method(:setHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_handler()"
    end
    # @param [true,false] arg0 
    # @return [void]
    def complete(arg0=nil)
      if !block_given? && arg0 == nil
        return @j_del.java_method(:complete, []).call()
      elsif (arg0.class == TrueClass || arg0.class == FalseClass) && !block_given?
        return @j_del.java_method(:complete, [Java::JavaLang::Boolean.java_class]).call(arg0)
      end
      raise ArgumentError, "Invalid arguments when calling complete(arg0)"
    end
    # @overload fail(arg0)
    #   @param [Exception] arg0 
    # @overload fail(arg0)
    #   @param [String] arg0 
    # @return [void]
    def fail(param_1=nil)
      if param_1.is_a?(Exception) && !block_given?
        return @j_del.java_method(:fail, [Java::JavaLang::Throwable.java_class]).call(::Vertx::Util::Utils.to_throwable(param_1))
      elsif param_1.class == String && !block_given?
        return @j_del.java_method(:fail, [Java::java.lang.String.java_class]).call(param_1)
      end
      raise ArgumentError, "Invalid arguments when calling fail(param_1)"
    end
    # @return [true,false]
    def result?
      if !block_given?
        return @j_del.java_method(:result, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling result?()"
    end
    # @return [Exception]
    def cause
      if !block_given?
        return ::Vertx::Util::Utils.from_throwable(@j_del.java_method(:cause, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling cause()"
    end
    # @return [true,false]
    def succeeded?
      if !block_given?
        return @j_del.java_method(:succeeded, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling succeeded?()"
    end
    # @return [true,false]
    def failed?
      if !block_given?
        return @j_del.java_method(:failed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling failed?()"
    end
    # @overload compose(mapper)
    #   @yield 
    # @overload compose(handler,next)
    #   @param [Proc] handler 
    #   @param [::Vertx::Future] _next 
    # @return [::Vertx::Future]
    def compose(param_1=nil,param_2=nil)
      if block_given? && param_1 == nil && param_2 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:compose, [Java::JavaUtilFunction::Function.java_class]).call((Proc.new { |event| yield(event).j_del })),::Vertx::Future)
      elsif param_1.class == Proc && param_2.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:compose, [Java::IoVertxCore::Handler.java_class,Java::IoVertxCore::Future.java_class]).call((Proc.new { |event| param_1.call(event) }),param_2.j_del),::Vertx::Future)
      end
      raise ArgumentError, "Invalid arguments when calling compose(param_1,param_2)"
    end
    # @overload map(mapper)
    #   @yield 
    # @overload map(value)
    #   @param [Object] value 
    # @return [::Vertx::Future]
    def map(param_1=nil)
      if block_given? && param_1 == nil
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:map, [Java::JavaUtilFunction::Function.java_class]).call((Proc.new { |event| ::Vertx::Util::Utils.to_object(yield(event)) })),::Vertx::Future)
      elsif (param_1.class == String  || param_1.class == Hash || param_1.class == Array || param_1.class == NilClass || param_1.class == TrueClass || param_1.class == FalseClass || param_1.class == Fixnum || param_1.class == Float) && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:map, [Java::java.lang.Object.java_class]).call(::Vertx::Util::Utils.to_object(param_1)),::Vertx::Future)
      end
      raise ArgumentError, "Invalid arguments when calling map(param_1)"
    end
    # @return [Proc]
    def completer
      if !block_given?
        if @cached_completer != nil
          return @cached_completer
        end
        return @cached_completer = ::Vertx::Util::Utils.to_async_result_handler_proc(@j_del.java_method(:completer, []).call()) { |val| val }
      end
      raise ArgumentError, "Invalid arguments when calling completer()"
    end
    # @return [:SOCKET_CREATED,:SOCKET_CLOSED,:SEND,:PUBLISH,:RECEIVE,:REGISTER,:UNREGISTER] the type of the event
    def type
      if !block_given?
        if @cached_type != nil
          return @cached_type
        end
        return @cached_type = @j_del.java_method(:type, []).call().name.intern
      end
      raise ArgumentError, "Invalid arguments when calling type()"
    end
    #  Use {::VertxWeb::BridgeEvent#get_raw_message} instead, will be removed in 3.3
    # @return [Hash{String => Object}]
    def raw_message
      if !block_given?
        if @cached_raw_message != nil
          return @cached_raw_message
        end
        return @cached_raw_message = @j_del.java_method(:rawMessage, []).call() != nil ? JSON.parse(@j_del.java_method(:rawMessage, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling raw_message()"
    end
    #  Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
    #  no message involved. If the returned message is modified, {::VertxWeb::BridgeEvent#set_raw_message} should be called with the
    #  new message.
    # @return [Hash{String => Object}] the raw JSON message for the event
    def get_raw_message
      if !block_given?
        return @j_del.java_method(:getRawMessage, []).call() != nil ? JSON.parse(@j_del.java_method(:getRawMessage, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_raw_message()"
    end
    #  Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
    #  no message involved.
    # @param [Hash{String => Object}] message the raw message
    # @return [self]
    def set_raw_message(message=nil)
      if message.class == Hash && !block_given?
        @j_del.java_method(:setRawMessage, [Java::IoVertxCoreJson::JsonObject.java_class]).call(::Vertx::Util::Utils.to_json_object(message))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_raw_message(message)"
    end
    #  Get the SockJSSocket instance corresponding to the event
    # @return [::VertxWeb::SockJSSocket] the SockJSSocket instance
    def socket
      if !block_given?
        if @cached_socket != nil
          return @cached_socket
        end
        return @cached_socket = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:socket, []).call(),::VertxWeb::SockJSSocket)
      end
      raise ArgumentError, "Invalid arguments when calling socket()"
    end
  end
end
