require 'vertx/vertx'
require 'vertx/future'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.healthchecks.HealthChecks
module VertxHealthChecks
  class HealthChecks
    # @private
    # @param j_del [::VertxHealthChecks::HealthChecks] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxHealthChecks::HealthChecks] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == HealthChecks
    end
    def @@j_api_type.wrap(obj)
      HealthChecks.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtHealthchecks::HealthChecks.java_class
    end
    #  Creates a new instance of the default implementation of {::VertxHealthChecks::HealthChecks}.
    # @param [::Vertx::Vertx] vertx the instance of Vert.x, must not be <code>null</code>
    # @return [::VertxHealthChecks::HealthChecks] the created instance
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtHealthchecks::HealthChecks.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxHealthChecks::HealthChecks)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{vertx})"
    end
    #  Registers a health check procedure.
    #  <p>
    #  The procedure is a  taking a  of {Hash} as parameter.
    #  Procedures are asynchronous, and <strong>must</strong> complete or fail the given .
    #  If the future object is failed, the procedure outcome is considered as `DOWN`. If the future is
    #  completed without any object, the procedure outcome is considered as `UP`. If the future is completed
    #  with a (not-null) {Hash}, the procedure outcome is the received status.
    # @param [String] name the name of the procedure, must not be <code>null</code> or empty
    # @yield the procedure, must not be <code>null</code>
    # @return [self]
    def register(name=nil)
      if name.class == String && block_given?
        @j_del.java_method(:register, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(name,(Proc.new { |event| yield(::Vertx::Util::Utils.safe_create(event,::Vertx::Future,::Vertx::Util::data_object_type(Java::IoVertxExtHealthchecks::Status))) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling register(#{name})"
    end
    #  Unregisters a procedure.
    # @param [String] name the name of the procedure
    # @return [self]
    def unregister(name=nil)
      if name.class == String && !block_given?
        @j_del.java_method(:unregister, [Java::java.lang.String.java_class]).call(name)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling unregister(#{name})"
    end
    #  Invokes the registered procedure with the given name and sub-procedures. It computes the overall
    #  outcome.
    # @param [String] name 
    # @yield the result handler, must not be <code>null</code>. The handler received an  marked as failed if the procedure with the given name cannot be found or invoked.
    # @return [self]
    def invoke(name=nil)
      if block_given? && name == nil
        @j_del.java_method(:invoke, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(event != nil ? JSON.parse(event.encode) : nil) }))
        return self
      elsif name.class == String && block_given?
        @j_del.java_method(:invoke, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(name,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling invoke(#{name})"
    end
  end
end
