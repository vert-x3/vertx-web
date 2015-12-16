require 'vertx/vertx'
require 'test/test_connection'
require 'test/test_connection_with_close_future'
require 'vertx/util/utils.rb'
# Generated from io.vertx.serviceproxy.testmodel.TestService
module Test
  #  @author <a href="http://tfox.org">Tim Fox</a>
  class TestService
    # @private
    # @param j_del [::Test::TestService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Test::TestService] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::Vertx::Vertx] vertx 
    # @return [::Test::TestService]
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxServiceproxyTestmodel::TestService.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::Test::TestService)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx)"
    end
    # @param [::Vertx::Vertx] vertx 
    # @param [String] address 
    # @return [::Test::TestService]
    def self.create_proxy(vertx=nil,address=nil)
      if vertx.class.method_defined?(:j_del) && address.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxServiceproxyTestmodel::TestService.java_method(:createProxy, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,address),::Test::TestService)
      end
      raise ArgumentError, "Invalid arguments when calling create_proxy(vertx,address)"
    end
    # @param [::Vertx::Vertx] vertx 
    # @param [String] address 
    # @return [::Test::TestService]
    def self.create_proxy_long_delivery(vertx=nil,address=nil)
      if vertx.class.method_defined?(:j_del) && address.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxServiceproxyTestmodel::TestService.java_method(:createProxyLongDelivery, [Java::IoVertxCore::Vertx.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,address),::Test::TestService)
      end
      raise ArgumentError, "Invalid arguments when calling create_proxy_long_delivery(vertx,address)"
    end
    # @yield 
    # @return [void]
    def long_delivery_success
      if block_given?
        return @j_del.java_method(:longDeliverySuccess, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling long_delivery_success()"
    end
    # @yield 
    # @return [void]
    def long_delivery_failed
      if block_given?
        return @j_del.java_method(:longDeliveryFailed, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling long_delivery_failed()"
    end
    # @param [String] str 
    # @yield 
    # @return [void]
    def create_connection(str=nil)
      if str.class == String && block_given?
        return @j_del.java_method(:createConnection, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(str,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::Test::TestConnection) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling create_connection(str)"
    end
    # @yield 
    # @return [void]
    def create_connection_with_close_future
      if block_given?
        return @j_del.java_method(:createConnectionWithCloseFuture, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::Test::TestConnectionWithCloseFuture) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling create_connection_with_close_future()"
    end
    # @return [void]
    def no_params
      if !block_given?
        return @j_del.java_method(:noParams, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling no_params()"
    end
    # @param [String] str 
    # @param [Fixnum] b 
    # @param [Fixnum] s 
    # @param [Fixnum] i 
    # @param [Fixnum] l 
    # @param [Float] f 
    # @param [Float] d 
    # @param [Fixnum] c 
    # @param [true,false] bool 
    # @return [void]
    def basic_types(str=nil,b=nil,s=nil,i=nil,l=nil,f=nil,d=nil,c=nil,bool=nil)
      if str.class == String && b.class == Fixnum && s.class == Fixnum && i.class == Fixnum && l.class == Fixnum && f.class == Float && d.class == Float && c.class == Fixnum && (bool.class == TrueClass || bool.class == FalseClass) && !block_given?
        return @j_del.java_method(:basicTypes, [Java::java.lang.String.java_class,Java::byte.java_class,Java::short.java_class,Java::int.java_class,Java::long.java_class,Java::float.java_class,Java::double.java_class,Java::char.java_class,Java::boolean.java_class]).call(str,::Vertx::Util::Utils.to_byte(b),::Vertx::Util::Utils.to_short(s),i,l,::Vertx::Util::Utils.to_float(f),::Vertx::Util::Utils.to_double(d),c,bool)
      end
      raise ArgumentError, "Invalid arguments when calling basic_types(str,b,s,i,l,f,d,c,bool)"
    end
    # @param [String] str 
    # @param [Fixnum] b 
    # @param [Fixnum] s 
    # @param [Fixnum] i 
    # @param [Fixnum] l 
    # @param [Float] f 
    # @param [Float] d 
    # @param [Fixnum] c 
    # @param [true,false] bool 
    # @return [void]
    def basic_boxed_types(str=nil,b=nil,s=nil,i=nil,l=nil,f=nil,d=nil,c=nil,bool=nil)
      if str.class == String && b.class == Fixnum && s.class == Fixnum && i.class == Fixnum && l.class == Fixnum && f.class == Float && d.class == Float && c.class == Fixnum && (bool.class == TrueClass || bool.class == FalseClass) && !block_given?
        return @j_del.java_method(:basicBoxedTypes, [Java::java.lang.String.java_class,Java::JavaLang::Byte.java_class,Java::JavaLang::Short.java_class,Java::JavaLang::Integer.java_class,Java::JavaLang::Long.java_class,Java::JavaLang::Float.java_class,Java::JavaLang::Double.java_class,Java::JavaLang::Character.java_class,Java::JavaLang::Boolean.java_class]).call(str,::Vertx::Util::Utils.to_byte(b),::Vertx::Util::Utils.to_short(s),::Vertx::Util::Utils.to_integer(i),l,::Vertx::Util::Utils.to_float(f),::Vertx::Util::Utils.to_double(d),c,bool)
      end
      raise ArgumentError, "Invalid arguments when calling basic_boxed_types(str,b,s,i,l,f,d,c,bool)"
    end
    # @param [String] str 
    # @param [Fixnum] b 
    # @param [Fixnum] s 
    # @param [Fixnum] i 
    # @param [Fixnum] l 
    # @param [Float] f 
    # @param [Float] d 
    # @param [Fixnum] c 
    # @param [true,false] bool 
    # @return [void]
    def basic_boxed_types_null(str=nil,b=nil,s=nil,i=nil,l=nil,f=nil,d=nil,c=nil,bool=nil)
      if str.class == String && b.class == Fixnum && s.class == Fixnum && i.class == Fixnum && l.class == Fixnum && f.class == Float && d.class == Float && c.class == Fixnum && (bool.class == TrueClass || bool.class == FalseClass) && !block_given?
        return @j_del.java_method(:basicBoxedTypesNull, [Java::java.lang.String.java_class,Java::JavaLang::Byte.java_class,Java::JavaLang::Short.java_class,Java::JavaLang::Integer.java_class,Java::JavaLang::Long.java_class,Java::JavaLang::Float.java_class,Java::JavaLang::Double.java_class,Java::JavaLang::Character.java_class,Java::JavaLang::Boolean.java_class]).call(str,::Vertx::Util::Utils.to_byte(b),::Vertx::Util::Utils.to_short(s),::Vertx::Util::Utils.to_integer(i),l,::Vertx::Util::Utils.to_float(f),::Vertx::Util::Utils.to_double(d),c,bool)
      end
      raise ArgumentError, "Invalid arguments when calling basic_boxed_types_null(str,b,s,i,l,f,d,c,bool)"
    end
    # @param [Hash{String => Object}] jsonObject 
    # @param [Array<String,Object>] jsonArray 
    # @return [void]
    def json_types(jsonObject=nil,jsonArray=nil)
      if jsonObject.class == Hash && jsonArray.class == Array && !block_given?
        return @j_del.java_method(:jsonTypes, [Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCoreJson::JsonArray.java_class]).call(::Vertx::Util::Utils.to_json_object(jsonObject),::Vertx::Util::Utils.to_json_array(jsonArray))
      end
      raise ArgumentError, "Invalid arguments when calling json_types(jsonObject,jsonArray)"
    end
    # @param [Hash{String => Object}] jsonObject 
    # @param [Array<String,Object>] jsonArray 
    # @return [void]
    def json_types_null(jsonObject=nil,jsonArray=nil)
      if jsonObject.class == Hash && jsonArray.class == Array && !block_given?
        return @j_del.java_method(:jsonTypesNull, [Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCoreJson::JsonArray.java_class]).call(::Vertx::Util::Utils.to_json_object(jsonObject),::Vertx::Util::Utils.to_json_array(jsonArray))
      end
      raise ArgumentError, "Invalid arguments when calling json_types_null(jsonObject,jsonArray)"
    end
    # @param [:FOO,:BAR,:WIBBLE] someEnum 
    # @return [void]
    def enum_type(someEnum=nil)
      if someEnum.class == Symbol && !block_given?
        return @j_del.java_method(:enumType, [Java::IoVertxServiceproxyTestmodel::SomeEnum.java_class]).call(Java::IoVertxServiceproxyTestmodel::SomeEnum.valueOf(someEnum))
      end
      raise ArgumentError, "Invalid arguments when calling enum_type(someEnum)"
    end
    # @param [:FOO,:BAR,:WIBBLE] someEnum 
    # @return [void]
    def enum_type_null(someEnum=nil)
      if someEnum.class == Symbol && !block_given?
        return @j_del.java_method(:enumTypeNull, [Java::IoVertxServiceproxyTestmodel::SomeEnum.java_class]).call(Java::IoVertxServiceproxyTestmodel::SomeEnum.valueOf(someEnum))
      end
      raise ArgumentError, "Invalid arguments when calling enum_type_null(someEnum)"
    end
    # @yield 
    # @return [void]
    def enum_type_as_result
      if block_given?
        return @j_del.java_method(:enumTypeAsResult, [Java::IoVertxCore::Handler.java_class]).call(nil)
      end
      raise ArgumentError, "Invalid arguments when calling enum_type_as_result()"
    end
    # @yield 
    # @return [void]
    def enum_type_as_result_null
      if block_given?
        return @j_del.java_method(:enumTypeAsResultNull, [Java::IoVertxCore::Handler.java_class]).call(nil)
      end
      raise ArgumentError, "Invalid arguments when calling enum_type_as_result_null()"
    end
    # @param [Hash] options 
    # @return [void]
    def data_object_type(options=nil)
      if options.class == Hash && !block_given?
        return @j_del.java_method(:dataObjectType, [Java::IoVertxServiceproxyTestmodel::TestDataObject.java_class]).call(Java::IoVertxServiceproxyTestmodel::TestDataObject.new(::Vertx::Util::Utils.to_json_object(options)))
      end
      raise ArgumentError, "Invalid arguments when calling data_object_type(options)"
    end
    # @param [Hash] options 
    # @return [void]
    def data_object_type_null(options=nil)
      if options.class == Hash && !block_given?
        return @j_del.java_method(:dataObjectTypeNull, [Java::IoVertxServiceproxyTestmodel::TestDataObject.java_class]).call(Java::IoVertxServiceproxyTestmodel::TestDataObject.new(::Vertx::Util::Utils.to_json_object(options)))
      end
      raise ArgumentError, "Invalid arguments when calling data_object_type_null(options)"
    end
    # @param [Array<String>] listString 
    # @param [Array<Fixnum>] listByte 
    # @param [Array<Fixnum>] listShort 
    # @param [Array<Fixnum>] listInt 
    # @param [Array<Fixnum>] listLong 
    # @param [Array<Hash{String => Object}>] listJsonObject 
    # @param [Array<Array<String,Object>>] listJsonArray 
    # @param [Array<Hash>] listDataObject 
    # @return [void]
    def list_params(listString=nil,listByte=nil,listShort=nil,listInt=nil,listLong=nil,listJsonObject=nil,listJsonArray=nil,listDataObject=nil)
      if listString.class == Array && listByte.class == Array && listShort.class == Array && listInt.class == Array && listLong.class == Array && listJsonObject.class == Array && listJsonArray.class == Array && listDataObject.class == Array && !block_given?
        return @j_del.java_method(:listParams, [Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class,Java::JavaUtil::List.java_class]).call(listString.map { |element| element },listByte.map { |element| ::Vertx::Util::Utils.to_byte(element) },listShort.map { |element| ::Vertx::Util::Utils.to_short(element) },listInt.map { |element| ::Vertx::Util::Utils.to_integer(element) },listLong.map { |element| element },listJsonObject.map { |element| ::Vertx::Util::Utils.to_json_object(element) },listJsonArray.map { |element| ::Vertx::Util::Utils.to_json_array(element) },listDataObject.map { |element| Java::IoVertxServiceproxyTestmodel::TestDataObject.new(::Vertx::Util::Utils.to_json_object(element)) })
      end
      raise ArgumentError, "Invalid arguments when calling list_params(listString,listByte,listShort,listInt,listLong,listJsonObject,listJsonArray,listDataObject)"
    end
    # @param [Set<String>] setString 
    # @param [Set<Fixnum>] setByte 
    # @param [Set<Fixnum>] setShort 
    # @param [Set<Fixnum>] setInt 
    # @param [Set<Fixnum>] setLong 
    # @param [Set<Hash{String => Object}>] setJsonObject 
    # @param [Set<Array<String,Object>>] setJsonArray 
    # @param [Set<Hash>] setDataObject 
    # @return [void]
    def set_params(setString=nil,setByte=nil,setShort=nil,setInt=nil,setLong=nil,setJsonObject=nil,setJsonArray=nil,setDataObject=nil)
      if setString.class == Set && setByte.class == Set && setShort.class == Set && setInt.class == Set && setLong.class == Set && setJsonObject.class == Set && setJsonArray.class == Set && setDataObject.class == Set && !block_given?
        return @j_del.java_method(:setParams, [Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class,Java::JavaUtil::Set.java_class]).call(Java::JavaUtil::LinkedHashSet.new(setString.map { |element| element }),Java::JavaUtil::LinkedHashSet.new(setByte.map { |element| ::Vertx::Util::Utils.to_byte(element) }),Java::JavaUtil::LinkedHashSet.new(setShort.map { |element| ::Vertx::Util::Utils.to_short(element) }),Java::JavaUtil::LinkedHashSet.new(setInt.map { |element| ::Vertx::Util::Utils.to_integer(element) }),Java::JavaUtil::LinkedHashSet.new(setLong.map { |element| element }),Java::JavaUtil::LinkedHashSet.new(setJsonObject.map { |element| ::Vertx::Util::Utils.to_json_object(element) }),Java::JavaUtil::LinkedHashSet.new(setJsonArray.map { |element| ::Vertx::Util::Utils.to_json_array(element) }),Java::JavaUtil::LinkedHashSet.new(setDataObject.map { |element| Java::IoVertxServiceproxyTestmodel::TestDataObject.new(::Vertx::Util::Utils.to_json_object(element)) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_params(setString,setByte,setShort,setInt,setLong,setJsonObject,setJsonArray,setDataObject)"
    end
    # @param [Hash{String => String}] mapString 
    # @param [Hash{String => Fixnum}] mapByte 
    # @param [Hash{String => Fixnum}] mapShort 
    # @param [Hash{String => Fixnum}] mapInt 
    # @param [Hash{String => Fixnum}] mapLong 
    # @param [Hash{String => Hash{String => Object}}] mapJsonObject 
    # @param [Hash{String => Array<String,Object>}] mapJsonArray 
    # @return [void]
    def map_params(mapString=nil,mapByte=nil,mapShort=nil,mapInt=nil,mapLong=nil,mapJsonObject=nil,mapJsonArray=nil)
      if mapString.class == Hash && mapByte.class == Hash && mapShort.class == Hash && mapInt.class == Hash && mapLong.class == Hash && mapJsonObject.class == Hash && mapJsonArray.class == Hash && !block_given?
        return @j_del.java_method(:mapParams, [Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class,Java::JavaUtil::Map.java_class]).call(Hash[mapString.map { |k,v| [k,v] }],Hash[mapByte.map { |k,v| [k,::Vertx::Util::Utils.to_byte(v)] }],Hash[mapShort.map { |k,v| [k,::Vertx::Util::Utils.to_short(v)] }],Hash[mapInt.map { |k,v| [k,::Vertx::Util::Utils.to_integer(v)] }],Hash[mapLong.map { |k,v| [k,v] }],Hash[mapJsonObject.map { |k,v| [k,::Vertx::Util::Utils.to_json_object(v)] }],Hash[mapJsonArray.map { |k,v| [k,::Vertx::Util::Utils.to_json_array(v)] }])
      end
      raise ArgumentError, "Invalid arguments when calling map_params(mapString,mapByte,mapShort,mapInt,mapLong,mapJsonObject,mapJsonArray)"
    end
    # @yield 
    # @return [void]
    def string_handler
      if block_given?
        return @j_del.java_method(:stringHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling string_handler()"
    end
    # @yield 
    # @return [void]
    def string_null_handler
      if block_given?
        return @j_del.java_method(:stringNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling string_null_handler()"
    end
    # @yield 
    # @return [void]
    def byte_handler
      if block_given?
        return @j_del.java_method(:byteHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling byte_handler()"
    end
    # @yield 
    # @return [void]
    def byte_null_handler
      if block_given?
        return @j_del.java_method(:byteNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling byte_null_handler()"
    end
    # @yield 
    # @return [void]
    def short_handler
      if block_given?
        return @j_del.java_method(:shortHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling short_handler()"
    end
    # @yield 
    # @return [void]
    def short_null_handler
      if block_given?
        return @j_del.java_method(:shortNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling short_null_handler()"
    end
    # @yield 
    # @return [void]
    def int_handler
      if block_given?
        return @j_del.java_method(:intHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling int_handler()"
    end
    # @yield 
    # @return [void]
    def int_null_handler
      if block_given?
        return @j_del.java_method(:intNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling int_null_handler()"
    end
    # @yield 
    # @return [void]
    def long_handler
      if block_given?
        return @j_del.java_method(:longHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling long_handler()"
    end
    # @yield 
    # @return [void]
    def long_null_handler
      if block_given?
        return @j_del.java_method(:longNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling long_null_handler()"
    end
    # @yield 
    # @return [void]
    def float_handler
      if block_given?
        return @j_del.java_method(:floatHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling float_handler()"
    end
    # @yield 
    # @return [void]
    def float_null_handler
      if block_given?
        return @j_del.java_method(:floatNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling float_null_handler()"
    end
    # @yield 
    # @return [void]
    def double_handler
      if block_given?
        return @j_del.java_method(:doubleHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling double_handler()"
    end
    # @yield 
    # @return [void]
    def double_null_handler
      if block_given?
        return @j_del.java_method(:doubleNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling double_null_handler()"
    end
    # @yield 
    # @return [void]
    def char_handler
      if block_given?
        return @j_del.java_method(:charHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling char_handler()"
    end
    # @yield 
    # @return [void]
    def char_null_handler
      if block_given?
        return @j_del.java_method(:charNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling char_null_handler()"
    end
    # @yield 
    # @return [void]
    def boolean_handler
      if block_given?
        return @j_del.java_method(:booleanHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling boolean_handler()"
    end
    # @yield 
    # @return [void]
    def boolean_null_handler
      if block_given?
        return @j_del.java_method(:booleanNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling boolean_null_handler()"
    end
    # @yield 
    # @return [void]
    def json_object_handler
      if block_given?
        return @j_del.java_method(:jsonObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling json_object_handler()"
    end
    # @yield 
    # @return [void]
    def json_object_null_handler
      if block_given?
        return @j_del.java_method(:jsonObjectNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling json_object_null_handler()"
    end
    # @yield 
    # @return [void]
    def json_array_handler
      if block_given?
        return @j_del.java_method(:jsonArrayHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling json_array_handler()"
    end
    # @yield 
    # @return [void]
    def json_array_null_handler
      if block_given?
        return @j_del.java_method(:jsonArrayNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling json_array_null_handler()"
    end
    # @yield 
    # @return [void]
    def data_object_handler
      if block_given?
        return @j_del.java_method(:dataObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling data_object_handler()"
    end
    # @yield 
    # @return [void]
    def data_object_null_handler
      if block_given?
        return @j_del.java_method(:dataObjectNullHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling data_object_null_handler()"
    end
    # @yield 
    # @return [void]
    def void_handler
      if block_given?
        return @j_del.java_method(:voidHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling void_handler()"
    end
    # @param [String] str 
    # @yield 
    # @return [self]
    def fluent_method(str=nil)
      if str.class == String && block_given?
        @j_del.java_method(:fluentMethod, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(str,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling fluent_method(str)"
    end
    # @return [self]
    def fluent_no_params
      if !block_given?
        @j_del.java_method(:fluentNoParams, []).call()
        return self
      end
      raise ArgumentError, "Invalid arguments when calling fluent_no_params()"
    end
    # @yield 
    # @return [void]
    def failing_method
      if block_given?
        return @j_del.java_method(:failingMethod, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling failing_method()"
    end
    # @param [Hash{String => Object}] object 
    # @param [String] str 
    # @param [Fixnum] i 
    # @param [Fixnum] chr 
    # @param [:FOO,:BAR,:WIBBLE] senum 
    # @yield 
    # @return [void]
    def invoke_with_message(object=nil,str=nil,i=nil,chr=nil,senum=nil)
      if object.class == Hash && str.class == String && i.class == Fixnum && chr.class == Fixnum && senum.class == Symbol && block_given?
        return @j_del.java_method(:invokeWithMessage, [Java::IoVertxCoreJson::JsonObject.java_class,Java::java.lang.String.java_class,Java::int.java_class,Java::char.java_class,Java::IoVertxServiceproxyTestmodel::SomeEnum.java_class,Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_json_object(object),str,i,chr,Java::IoVertxServiceproxyTestmodel::SomeEnum.valueOf(senum),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling invoke_with_message(object,str,i,chr,senum)"
    end
    # @yield 
    # @return [void]
    def list_string_handler
      if block_given?
        return @j_del.java_method(:listStringHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_string_handler()"
    end
    # @yield 
    # @return [void]
    def list_byte_handler
      if block_given?
        return @j_del.java_method(:listByteHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_byte_handler()"
    end
    # @yield 
    # @return [void]
    def list_short_handler
      if block_given?
        return @j_del.java_method(:listShortHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_short_handler()"
    end
    # @yield 
    # @return [void]
    def list_int_handler
      if block_given?
        return @j_del.java_method(:listIntHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_int_handler()"
    end
    # @yield 
    # @return [void]
    def list_long_handler
      if block_given?
        return @j_del.java_method(:listLongHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_long_handler()"
    end
    # @yield 
    # @return [void]
    def list_float_handler
      if block_given?
        return @j_del.java_method(:listFloatHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_float_handler()"
    end
    # @yield 
    # @return [void]
    def list_double_handler
      if block_given?
        return @j_del.java_method(:listDoubleHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_double_handler()"
    end
    # @yield 
    # @return [void]
    def list_char_handler
      if block_given?
        return @j_del.java_method(:listCharHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_char_handler()"
    end
    # @yield 
    # @return [void]
    def list_bool_handler
      if block_given?
        return @j_del.java_method(:listBoolHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_bool_handler()"
    end
    # @yield 
    # @return [void]
    def list_json_object_handler
      if block_given?
        return @j_del.java_method(:listJsonObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt != nil ? JSON.parse(elt.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_json_object_handler()"
    end
    # @yield 
    # @return [void]
    def list_json_array_handler
      if block_given?
        return @j_del.java_method(:listJsonArrayHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt != nil ? JSON.parse(elt.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_json_array_handler()"
    end
    # @yield 
    # @return [void]
    def list_data_object_handler
      if block_given?
        return @j_del.java_method(:listDataObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt != nil ? JSON.parse(elt.toJson.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling list_data_object_handler()"
    end
    # @yield 
    # @return [void]
    def set_string_handler
      if block_given?
        return @j_del.java_method(:setStringHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_string_handler()"
    end
    # @yield 
    # @return [void]
    def set_byte_handler
      if block_given?
        return @j_del.java_method(:setByteHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_byte_handler()"
    end
    # @yield 
    # @return [void]
    def set_short_handler
      if block_given?
        return @j_del.java_method(:setShortHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_short_handler()"
    end
    # @yield 
    # @return [void]
    def set_int_handler
      if block_given?
        return @j_del.java_method(:setIntHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_int_handler()"
    end
    # @yield 
    # @return [void]
    def set_long_handler
      if block_given?
        return @j_del.java_method(:setLongHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_long_handler()"
    end
    # @yield 
    # @return [void]
    def set_float_handler
      if block_given?
        return @j_del.java_method(:setFloatHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_float_handler()"
    end
    # @yield 
    # @return [void]
    def set_double_handler
      if block_given?
        return @j_del.java_method(:setDoubleHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_double_handler()"
    end
    # @yield 
    # @return [void]
    def set_char_handler
      if block_given?
        return @j_del.java_method(:setCharHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_char_handler()"
    end
    # @yield 
    # @return [void]
    def set_bool_handler
      if block_given?
        return @j_del.java_method(:setBoolHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_bool_handler()"
    end
    # @yield 
    # @return [void]
    def set_json_object_handler
      if block_given?
        return @j_del.java_method(:setJsonObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt != nil ? JSON.parse(elt.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_json_object_handler()"
    end
    # @yield 
    # @return [void]
    def set_json_array_handler
      if block_given?
        return @j_del.java_method(:setJsonArrayHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt != nil ? JSON.parse(elt.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_json_array_handler()"
    end
    # @yield 
    # @return [void]
    def set_data_object_handler
      if block_given?
        return @j_del.java_method(:setDataObjectHandler, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.to_set(ar.result).map! { |elt| elt != nil ? JSON.parse(elt.toJson.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling set_data_object_handler()"
    end
    # @return [void]
    def ignored_method
      if !block_given?
        return @j_del.java_method(:ignoredMethod, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling ignored_method()"
    end
  end
end
