require 'vertx-apex/session_store'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.sstore.ClusteredSessionStore
module VertxApex
  #  A session store which stores sessions in a distributed map so they are available across the cluster.
  class ClusteredSessionStore < ::VertxApex::SessionStore
    # @private
    # @param j_del [::VertxApex::ClusteredSessionStore] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::ClusteredSessionStore] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a session store
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [String] sessionMapName the session map name
    # @return [::VertxApex::ClusteredSessionStore] the session store
    def self.create(vertx=nil,sessionMapName=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && sessionMapName == nil
        return ::VertxApex::ClusteredSessionStore.new(Java::IoVertxExtApexSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del))
      elsif vertx.class.method_defined?(:j_del) && sessionMapName.class == String && !block_given?
        return ::VertxApex::ClusteredSessionStore.new(Java::IoVertxExtApexSstore::ClusteredSessionStore.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,sessionMapName))
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,sessionMapName)"
    end
  end
end
