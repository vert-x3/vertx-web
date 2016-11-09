require 'vertx-web/parsed_header_value'
require 'vertx-web/locale'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.LanguageHeader
module VertxWeb
  #  A parsed language header.
  #  Delivers a more direct access to the individual elements of the header it represents
  class LanguageHeader < ::VertxWeb::Locale
    include ::VertxWeb::ParsedHeaderValue
    # @private
    # @param j_del [::VertxWeb::LanguageHeader] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::LanguageHeader] the underlying java delegate
    def j_del
      @j_del
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
    #  Contains the raw value that was received from the user agent 
    # @return [String]
    def raw_value
      if !block_given?
        return @j_del.java_method(:rawValue, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling raw_value()"
    end
    #  Holds the unparsed value of the header.<br>
    #  For the most part, this is the content before the semi-colon (";")
    # @return [String]
    def value
      if !block_given?
        return @j_del.java_method(:value, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling value()"
    end
    #  Holds the weight specified in the "q" parameter of the header.<br>
    #  If the parameter is not specified, 1.0 is assumed according to 
    #  <a href="https://tools.ietf.org/html/rfc7231#section-5.3.1">rfc7231</a>
    # @return [Float] 
    def weight
      if !block_given?
        return @j_del.java_method(:weight, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling weight()"
    end
    #  The value of the parameter specified by this key. Each is one of 3 things:
    #  <ol>
    #  <li>null &lt;- That key was not specified</li>
    #  <li>ParsedHeaderValue.EMPTY (tested using ==) &lt;- The value was not specified</li>
    #  <li>[Other] <- The value of the parameter</li>
    #  </ol>
    #  <b>Note:</b> The <code>q</code> parameter is never present.
    # @param [String] key 
    # @return [String] 
    def parameter(key=nil)
      if key.class == String && !block_given?
        return @j_del.java_method(:parameter, [Java::java.lang.String.java_class]).call(key)
      end
      raise ArgumentError, "Invalid arguments when calling parameter(key)"
    end
    #  The parameters specified in this header value.
    #  <b>Note:</b> The <code>q</code> parameter is never present.
    # @return [Hash{String => String}] Unmodifiable Map of parameters of this header value
    def parameters
      if !block_given?
        return Java::IoVertxLangRuby::Helper.adaptingMap(@j_del.java_method(:parameters, []).call(), Proc.new { |val| ::Vertx::Util::Utils.from_object(val) }, Proc.new { |val| ::Vertx::Util::Utils.to_string(val) })
      end
      raise ArgumentError, "Invalid arguments when calling parameters()"
    end
    #  Is this an allowed operation as specified by the corresponding header?
    # @return [true,false] 
    def permitted?
      if !block_given?
        return @j_del.java_method(:isPermitted, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling permitted?()"
    end
    #  Test if this header is matched by matchTry header 
    # @param [::VertxWeb::ParsedHeaderValue] matchTry The header to be matched from
    # @return [true,false] true if this header represents a subset of matchTry, otherwise, false
    def matched_by?(matchTry=nil)
      if matchTry.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:isMatchedBy, [Java::IoVertxExtWeb::ParsedHeaderValue.java_class]).call(matchTry.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling matched_by?(matchTry)"
    end
    #  An integer that represents the absolute order position of this header
    # @return [Fixnum]
    def weighted_order
      if !block_given?
        return @j_del.java_method(:weightedOrder, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling weighted_order()"
    end
    #  The tag of the language as specified by 
    #  <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>.<br>
    #  Equivalent to 
    # @return [String] The language tag
    def tag
      if !block_given?
        return @j_del.java_method(:tag, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling tag()"
    end
    #  A subtag of this language header.<br>
    #  + info: <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>
    # @param [Fixnum] level 
    # @return [String] The language subtag at specified position
    def subtag(level=nil)
      if !block_given? && level == nil
        return @j_del.java_method(:subtag, []).call()
      elsif level.class == Fixnum && !block_given?
        return @j_del.java_method(:subtag, [Java::int.java_class]).call(level)
      end
      raise ArgumentError, "Invalid arguments when calling subtag(level)"
    end
    # @return [Fixnum] the number of subtags this value has
    def subtag_count
      if !block_given?
        return @j_del.java_method(:subtagCount, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling subtag_count()"
    end
  end
end
