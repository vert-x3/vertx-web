require 'vertx-web/template_engine'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.ThymeleafTemplateEngine
module VertxWeb
  #  A template engine that uses the Thymeleaf library.
  class ThymeleafTemplateEngine < ::VertxWeb::TemplateEngine
    # @private
    # @param j_del [::VertxWeb::ThymeleafTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::ThymeleafTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a template engine using defaults
    # @return [::VertxWeb::ThymeleafTemplateEngine] the engine
    def self.create
      if !block_given?
        return ::VertxWeb::ThymeleafTemplateEngine.new(Java::IoVertxExtWebTempl::ThymeleafTemplateEngine.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the mode for the engine
    # @param [String] mode the mode
    # @return [::VertxWeb::ThymeleafTemplateEngine] a reference to this for fluency
    def set_mode(mode=nil)
      if mode.class == String && !block_given?
        return ::VertxWeb::ThymeleafTemplateEngine.new(@j_del.java_method(:setMode, [Java::java.lang.String.java_class]).call(mode))
      end
      raise ArgumentError, "Invalid arguments when calling set_mode(mode)"
    end
  end
end
