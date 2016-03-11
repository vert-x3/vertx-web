require 'vertx-web/template_engine'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.templ.Thymeleaf3TemplateEngine
module VertxWeb
  #  A template engine that uses the Thymeleaf library.
  class Thymeleaf3TemplateEngine < ::VertxWeb::TemplateEngine
    # @private
    # @param j_del [::VertxWeb::Thymeleaf3TemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Thymeleaf3TemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a template engine using defaults
    # @return [::VertxWeb::Thymeleaf3TemplateEngine] the engine
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::Thymeleaf3TemplateEngine.java_method(:create, []).call(),::VertxWeb::Thymeleaf3TemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the mode for the engine
    # @param [:HTML,:XML,:TEXT,:JAVASCRIPT,:CSS,:RAW,:HTML5,:LEGACYHTML5,:XHTML,:VALIDXHTML,:VALIDXML] mode the mode
    # @return [::VertxWeb::Thymeleaf3TemplateEngine] a reference to this for fluency
    def set_mode(mode=nil)
      if mode.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMode, [Java::OrgThymeleafTemplatemode::TemplateMode.java_class]).call(Java::OrgThymeleafTemplatemode::TemplateMode.valueOf(mode)),::VertxWeb::Thymeleaf3TemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_mode(mode)"
    end
  end
end
