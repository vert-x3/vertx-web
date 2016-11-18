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
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == TestBaseImportsService
    end
    def @@j_api_type.wrap(obj)
      TestBaseImportsService.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxServiceproxyTestmodel::TestBaseImportsService.java_class
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
