require 'vertx-web/session_store'
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
    #  Create a session store
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [String] sessionMapName the session map name
    # @return [::VertxWeb::ClusteredSessionStore] the session store
    def self.create(vertx=nil,sessionMapName=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && sessionMapName == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWeb::ClusteredSessionStore)
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,sessionMapName),::VertxWeb::ClusteredSessionStore)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,sessionMapName)"
    end
  end
end
