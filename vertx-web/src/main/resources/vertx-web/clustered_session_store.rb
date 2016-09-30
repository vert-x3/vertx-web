require 'vertx-web/session'
require 'vertx-web/session_store'
require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.sstore.ClusteredSessionStore
module VertxWeb
  #  A session store which stores sessions in a distributed map so they are available across the cluster.
  class ClusteredSessionStore < ::VertxWeb::SessionStore
    # @private
    # @param j_del [::VertxWeb::ClusteredSessionStore] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ClusteredSessionStore] the underlying java delegate
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
    #  Create a session store.<p/>
    # 
    #  The retry timeout value, configures how long the session handler will retry to get a session from the store
    #  when it is not found.
    # @overload create(vertx)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    # @overload create(vertx,sessionMapName)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [String] sessionMapName the session map name
    # @overload create(vertx,retryTimeout)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [Fixnum] retryTimeout the store retry timeout, in ms
    # @overload create(vertx,sessionMapName,retryTimeout)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [String] sessionMapName the session map name
    #   @param [Fixnum] retryTimeout the store retry timeout, in ms
    # @return [::VertxWeb::ClusteredSessionStore] the session store
    def self.create(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class.method_defined?(:j_del) && !block_given? && param_2 == nil && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(param_1.j_del),::VertxWeb::ClusteredSessionStore)
      elsif param_1.class.method_defined?(:j_del) && param_2.class == String && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(param_1.j_del,param_2),::VertxWeb::ClusteredSessionStore)
      elsif param_1.class.method_defined?(:j_del) && param_2.class == Fixnum && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::long.java_class]).call(param_1.j_del,param_2),::VertxWeb::ClusteredSessionStore)
      elsif param_1.class.method_defined?(:j_del) && param_2.class == String && param_3.class == Fixnum && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class,Java::long.java_class]).call(param_1.j_del,param_2,param_3),::VertxWeb::ClusteredSessionStore)
      end
      raise ArgumentError, "Invalid arguments when calling create(param_1,param_2,param_3)"
    end
  end
end
