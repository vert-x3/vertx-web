require 'vertx/buffer'
require 'vertx/util/utils.rb'
# Generated from io.vertx.webclient.PayloadCodec
module VertxWebClient
  #  A builder for configuring client-side HTTP responses.
  class PayloadCodec
    # @private
    # @param j_del [::VertxWebClient::PayloadCodec] the java delegate
    def initialize(j_del, j_arg_T=nil)
      @j_del = j_del
      @j_arg_T = j_arg_T != nil ? j_arg_T : ::Vertx::Util::unknown_type
    end
    # @private
    # @return [::VertxWebClient::PayloadCodec] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [String] enc 
    # @return [::VertxWebClient::PayloadCodec]
    def self.string(enc=nil)
      if !block_given? && enc == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::PayloadCodec.java_method(:string, []).call(),::VertxWebClient::PayloadCodec, nil)
      elsif enc.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::PayloadCodec.java_method(:string, [Java::java.lang.String.java_class]).call(enc),::VertxWebClient::PayloadCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling string(#{enc})"
    end
    # @return [::VertxWebClient::PayloadCodec]
    def self.buffer
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::PayloadCodec.java_method(:buffer, []).call(),::VertxWebClient::PayloadCodec,::Vertx::Buffer.j_api_type)
      end
      raise ArgumentError, "Invalid arguments when calling buffer()"
    end
    # @return [::VertxWebClient::PayloadCodec]
    def self.json_object
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWebclient::PayloadCodec.java_method(:jsonObject, []).call(),::VertxWebClient::PayloadCodec, nil)
      end
      raise ArgumentError, "Invalid arguments when calling json_object()"
    end
  end
end
