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
