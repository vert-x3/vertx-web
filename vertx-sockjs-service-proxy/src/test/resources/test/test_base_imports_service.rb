require 'vertx/util/utils.rb'
# Generated from io.vertx.serviceproxy.testmodel.TestBaseImportsService
module Test
  #  Test base imports are corrects.
  class TestBaseImportsService
    # @private
    # @param j_del [::Test::TestBaseImportsService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Test::TestBaseImportsService] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [void]
    def m
      if !block_given?
        return @j_del.java_method(:m, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling m()"
    end
  end
end
