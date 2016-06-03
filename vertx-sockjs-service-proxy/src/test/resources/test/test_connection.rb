require 'vertx/util/utils.rb'
# Generated from io.vertx.serviceproxy.testmodel.TestConnection
module Test
  #  @author <a href="http://tfox.org">Tim Fox</a>
  class TestConnection
    # @private
    # @param j_del [::Test::TestConnection] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Test::TestConnection] the underlying java delegate
    def j_del
      @j_del
    end
    # @yield 
    # @return [self]
    def start_transaction
      if block_given?
        @j_del.java_method(:startTransaction, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling start_transaction()"
    end
    # @param [String] name 
    # @param [Hash{String => Object}] data 
    # @yield 
    # @return [self]
    def insert(name=nil,data=nil)
      if name.class == String && data.class == Hash && block_given?
        @j_del.java_method(:insert, [Java::java.lang.String.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(name,::Vertx::Util::Utils.to_json_object(data),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling insert(name,data)"
    end
    # @yield 
    # @return [self]
    def commit
      if block_given?
        @j_del.java_method(:commit, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling commit()"
    end
    # @yield 
    # @return [self]
    def rollback
      if block_given?
        @j_del.java_method(:rollback, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling rollback()"
    end
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
  end
end
