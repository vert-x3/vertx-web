require 'vertx-apex/template_engine'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.templ.ThymeleafTemplateEngine
module VertxApex
  #  A template engine that uses the Thymeleaf library.
  class ThymeleafTemplateEngine < ::VertxApex::TemplateEngine
    # @private
    # @param j_del [::VertxApex::ThymeleafTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::ThymeleafTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a template engine using defaults
    # @return [::VertxApex::ThymeleafTemplateEngine] the engine
    def self.create
      if !block_given?
        return ::VertxApex::ThymeleafTemplateEngine.new(Java::IoVertxExtApexTempl::ThymeleafTemplateEngine.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the mode for the engine
    # @param [String] mode the mode
    # @return [::VertxApex::ThymeleafTemplateEngine] a reference to this for fluency
    def set_mode(mode=nil)
      if mode.class == String && !block_given?
        return ::VertxApex::ThymeleafTemplateEngine.new(@j_del.java_method(:setMode, [Java::java.lang.String.java_class]).call(mode))
      end
      raise ArgumentError, "Invalid arguments when calling set_mode(mode)"
    end
  end
end
