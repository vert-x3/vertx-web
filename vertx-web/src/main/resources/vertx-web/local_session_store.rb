require 'vertx-web/session'
require 'vertx-web/session_store'
require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.sstore.LocalSessionStore
module VertxWeb
  #  A session store which is only available on a single node.
  #  <p>
  #  Can be used when sticky sessions are being used.
  class LocalSessionStore < ::VertxWeb::SessionStore
    # @private
    # @param j_del [::VertxWeb::LocalSessionStore] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::LocalSessionStore] the underlying java delegate
    def j_del
      @j_del
    end
    #  The retry timeout value in milli seconds used by the session handler when it retrieves a value from the store.<p/>
    # 
    #  A non positive value means there is no retry at all.
    # @return [Fixnum] the timeout value, in ms
    def retry_timeout
      if !block_given?
        return @j_del.java_method(:retryTimeout, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling retry_timeout()"
    end
    #  Create a new session
    # @param [Fixnum] timeout - the session timeout, in ms
    # @return [::VertxWeb::Session] the session
    def create_session(timeout=nil)
      if timeout.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:createSession, [Java::long.java_class]).call(timeout),::VertxWeb::Session)
      end
      raise ArgumentError, "Invalid arguments when calling create_session(timeout)"
    end
    #  Get the session with the specified ID
    # @param [String] id the unique ID of the session
    # @yield will be called with a result holding the session, or a failure
    # @return [void]
    def get(id=nil)
      if id.class == String && block_given?
        return @j_del.java_method(:get, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(id,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxWeb::Session) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get(id)"
    end
    #  Delete the session with the specified ID
    # @param [String] id the unique ID of the session
    # @yield will be called with a result true/false, or a failure
    # @return [void]
    def delete(id=nil)
      if id.class == String && block_given?
        return @j_del.java_method(:delete, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(id,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling delete(id)"
    end
    #  Add a session with the specified ID
    # @param [::VertxWeb::Session] session the session
    # @yield will be called with a result true/false, or a failure
    # @return [void]
    def put(session=nil)
      if session.class.method_defined?(:j_del) && block_given?
        return @j_del.java_method(:put, [Java::IoVertxExtWeb::Session.java_class,Java::IoVertxCore::Handler.java_class]).call(session.j_del,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling put(session)"
    end
    #  Remove all sessions from the store
    # @yield will be called with a result true/false, or a failure
    # @return [void]
    def clear
      if block_given?
        return @j_del.java_method(:clear, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling clear()"
    end
    #  Get the number of sessions in the store
    # @yield will be called with the number, or a failure
    # @return [void]
    def size
      if block_given?
        return @j_del.java_method(:size, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling size()"
    end
    #  Close the store
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    #  Create a session store
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [String] sessionMapName name for map used to store sessions
    # @param [Fixnum] reaperInterval how often, in ms, to check for expired sessions
    # @return [::VertxWeb::LocalSessionStore] the session store
    def self.create(vertx=nil,sessionMapName=nil,reaperInterval=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && sessionMapName == nil && reaperInterval == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::LocalSessionStore)
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && !block_given? && reaperInterval == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,sessionMapName),::VertxWeb::LocalSessionStore)
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && reaperInterval.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::LocalSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class,Java::long.java_class]).call(vertx.j_del,sessionMapName,reaperInterval),::VertxWeb::LocalSessionStore)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,sessionMapName,reaperInterval)"
    end
  end
end
