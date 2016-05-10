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
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWebTempl::ThymeleafTemplateEngine.java_method(:create, []).call(),::VertxWeb::ThymeleafTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the mode for the engine
    # @param [:HTML,:XML,:TEXT,:JAVASCRIPT,:CSS,:RAW,:HTML5,:LEGACYHTML5,:XHTML,:VALIDXHTML,:VALIDXML] mode the mode
    # @return [::VertxWeb::ThymeleafTemplateEngine] a reference to this for fluency
    def set_mode(mode=nil)
      if mode.class == Symbol && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:setMode, [Java::OrgThymeleafTemplatemode::TemplateMode.java_class]).call(Java::OrgThymeleafTemplatemode::TemplateMode.valueOf(mode)),::VertxWeb::ThymeleafTemplateEngine)
      end
      raise ArgumentError, "Invalid arguments when calling set_mode(mode)"
    end
  end
end
