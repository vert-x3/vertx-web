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
