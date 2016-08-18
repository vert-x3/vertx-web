require 'vertx/util/utils.rb'
# Generated from io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture
module Test
  class TestConnectionWithCloseFuture
    # @private
    # @param j_del [::Test::TestConnectionWithCloseFuture] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Test::TestConnectionWithCloseFuture] the underlying java delegate
    def j_del
      @j_del
    end
    # @yield 
    # @return [void]
    def close
      if block_given?
        return @j_del.java_method(:close, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    # @yield 
    # @return [void]
    def some_method
      if block_given?
        return @j_del.java_method(:someMethod, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling some_method()"
    end
  end
end
