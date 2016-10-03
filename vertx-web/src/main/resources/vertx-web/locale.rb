require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.Locale
module VertxWeb
  class Locale
    # @private
    # @param j_del [::VertxWeb::Locale] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::Locale] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [String] language 
    # @param [String] country 
    # @param [String] variant 
    # @return [::VertxWeb::Locale]
    def self.create(language=nil,country=nil,variant=nil)
      if !block_given? && language == nil && country == nil && variant == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Locale.java_method(:create, []).call(),::VertxWeb::Locale)
      elsif language.class == String && !block_given? && country == nil && variant == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Locale.java_method(:create, [Java::java.lang.String.java_class]).call(language),::VertxWeb::Locale)
      elsif language.class == String && country.class == String && !block_given? && variant == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Locale.java_method(:create, [Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(language,country),::VertxWeb::Locale)
      elsif language.class == String && country.class == String && variant.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtWeb::Locale.java_method(:create, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class]).call(language,country,variant),::VertxWeb::Locale)
      end
      raise ArgumentError, "Invalid arguments when calling create(language,country,variant)"
    end
    #  Returns the language as reported by the HTTP client.
    # @return [String] language
    def language
      if !block_given?
        return @j_del.java_method(:language, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling language()"
    end
    #  Returns the country as reported by the HTTP client.
    # @return [String] variant
    def country
      if !block_given?
        return @j_del.java_method(:country, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling country()"
    end
    #  Returns the variant as reported by the HTTP client.
    # @return [String] variant
    def variant
      if !block_given?
        return @j_del.java_method(:variant, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling variant()"
    end
  end
end
