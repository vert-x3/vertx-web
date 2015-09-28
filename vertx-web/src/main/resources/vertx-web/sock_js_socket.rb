require 'vertx-web/session'
require 'vertx-auth-common/user'
require 'vertx/buffer'
require 'vertx/write_stream'
require 'vertx/read_stream'
require 'vertx/multi_map'
require 'vertx/socket_address'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.handler.sockjs.SockJSSocket
module VertxWeb
  # 
  #  You interact with SockJS clients through instances of SockJS socket.
  #  <p>
  #  The API is very similar to {::Vertx::WebSocket}.
  #  It implements both  and 
  #  so it can be used with
  #  {::Vertx::Pump} to pump data with flow control.<p>
  class SockJSSocket
    include ::Vertx::ReadStream
    include ::Vertx::WriteStream
    # @private
    # @param j_del [::VertxWeb::SockJSSocket] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::SockJSSocket] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [true,false]
    def write_queue_full?
      if !block_given?
        return @j_del.java_method(:writeQueueFull, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling write_queue_full?()"
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
    # @yield 
    # @return [self]
    def handler
      if block_given?
        @j_del.java_method(:handler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::Vertx::Buffer)) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling handler()"
    end
    # @return [self]
    def pause
      if !block_given?
        @j_del.java_method(:pause, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling pause()"
    end
    # @return [self]
    def resume
      if !block_given?
        @j_del.java_method(:resume, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling resume()"
    end
    # @yield 
    # @return [self]
    def end_handler
      if block_given?
        @j_del.java_method(:endHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
        return self
      end
      raise ArgumentError, "Invalid arguments when calling end_handler()"
    end
    # @param [::Vertx::Buffer] data 
    # @return [self]
    def write(data=nil)
      if data.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:write, [Java::IoVertxCoreBuffer::Buffer.java_class]).call(data.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling write(data)"
    end
    # @param [Fixnum] maxSize 
    # @return [self]
    def set_write_queue_max_size(maxSize=nil)
      if maxSize.class == Fixnum && !block_given?
        @j_del.java_method(:setWriteQueueMaxSize, [Java::int.java_class]).call(maxSize)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling set_write_queue_max_size(maxSize)"
    end
    # @yield 
    # @return [self]
    def drain_handler
      if block_given?
        @j_del.java_method(:drainHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
        return self
      end
      raise ArgumentError, "Invalid arguments when calling drain_handler()"
    end
    #  When a <code>SockJSSocket</code> is created it automatically registers an event handler with the event bus, the ID of that
    #  handler is given by <code>writeHandlerID</code>.
    #  <p>
    #  Given this ID, a different event loop can send a buffer to that event handler using the event bus and
    #  that buffer will be received by this instance in its own event loop and written to the underlying socket. This
    #  allows you to write data to other sockets which are owned by different event loops.
    # @return [String]
    def write_handler_id
      if !block_given?
        return @j_del.java_method(:writeHandlerID, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling write_handler_id()"
    end
    #  Close it
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    #  Return the remote address for this socket
    # @return [::Vertx::SocketAddress]
    def remote_address
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:remoteAddress, []).call(),::Vertx::SocketAddress)
      end
      raise ArgumentError, "Invalid arguments when calling remote_address()"
    end
    #  Return the local address for this socket
    # @return [::Vertx::SocketAddress]
    def local_address
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:localAddress, []).call(),::Vertx::SocketAddress)
      end
      raise ArgumentError, "Invalid arguments when calling local_address()"
    end
    #  Return the headers corresponding to the last request for this socket or the websocket handshake
    #  Any cookie headers will be removed for security reasons
    # @return [::Vertx::MultiMap]
    def headers
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:headers, []).call(),::Vertx::MultiMap)
      end
      raise ArgumentError, "Invalid arguments when calling headers()"
    end
    #  Return the URI corresponding to the last request for this socket or the websocket handshake
    # @return [String]
    def uri
      if !block_given?
        return @j_del.java_method(:uri, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling uri()"
    end
    #  @return the Vert.x-Web session corresponding to this socket
    # @return [::VertxWeb::Session]
    def web_session
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:webSession, []).call(),::VertxWeb::Session)
      end
      raise ArgumentError, "Invalid arguments when calling web_session()"
    end
    #   @return the Vert.x-Web user corresponding to this socket
    # @return [::VertxAuthCommon::User]
    def web_user
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:webUser, []).call(),::VertxAuthCommon::User)
      end
      raise ArgumentError, "Invalid arguments when calling web_user()"
    end
  end
end
