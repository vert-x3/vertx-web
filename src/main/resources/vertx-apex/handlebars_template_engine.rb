require 'vertx-apex/template_engine'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.apex.templ.HandlebarsTemplateEngine
module VertxApex
  #  A template engine that uses the Handlebars library.
  class HandlebarsTemplateEngine < ::VertxApex::TemplateEngine
    # @private
    # @param j_del [::VertxApex::HandlebarsTemplateEngine] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxApex::HandlebarsTemplateEngine] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a template engine using defaults
    # @return [::VertxApex::HandlebarsTemplateEngine] the engine
    def self.create
      if !block_given?
        return ::VertxApex::HandlebarsTemplateEngine.new(Java::IoVertxExtApexTempl::HandlebarsTemplateEngine.java_method(:create, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    #  Set the extension for the engine
    # @param [String] extension the extension
    # @return [::VertxApex::HandlebarsTemplateEngine] a reference to this for fluency
    def set_extension(extension=nil)
      if extension.class == String && !block_given?
        return ::VertxApex::HandlebarsTemplateEngine.new(@j_del.java_method(:setExtension, [Java::java.lang.String.java_class]).call(extension))
      end
      raise ArgumentError, "Invalid arguments when calling set_extension(extension)"
    end
    #  Set the max cache size for the engine
    # @param [Fixnum] maxCacheSize the maxCacheSize
    # @return [::VertxApex::HandlebarsTemplateEngine] a reference to this for fluency
    def set_max_cache_size(maxCacheSize=nil)
      if maxCacheSize.class == Fixnum && !block_given?
        return ::VertxApex::HandlebarsTemplateEngine.new(@j_del.java_method(:setMaxCacheSize, [Java::int.java_class]).call(maxCacheSize))
      end
      raise ArgumentError, "Invalid arguments when calling set_max_cache_size(maxCacheSize)"
    end
  end
end
