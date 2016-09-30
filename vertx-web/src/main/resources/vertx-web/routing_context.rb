require 'vertx-web/route'
require 'vertx-web/cookie'
require 'vertx-web/locale'
require 'vertx-web/file_upload'
require 'vertx/http_server_request'
require 'vertx-web/session'
require 'vertx-auth-common/user'
require 'vertx/buffer'
require 'vertx/http_server_response'
require 'vertx/vertx'
require 'vertx-web/parsed_header_values'
require 'vertx-web/language_header'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.web.RoutingContext
module VertxWeb
  #  Represents the context for the handling of a request in Vert.x-Web.
  #  <p>
  #  A new instance is created for each HTTP request that is received in the
  #  {::VertxWeb::Router#accept} of the router.
  #  <p>
  #  The same instance is passed to any matching request or failure handlers during the routing of the request or
  #  failure.
  #  <p>
  #  The context provides access to the  and 
  #  and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
  #  have been routed to the handler for the request.
  #  <p>
  #  The context also provides access to the {::VertxWeb::Session}, cookies and body for the request, given the correct handlers
  #  in the application.
  class RoutingContext
    # @private
    # @param j_del [::VertxWeb::RoutingContext] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWeb::RoutingContext] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [::Vertx::HttpServerRequest] the HTTP request object
    def request
      if !block_given?
        if @cached_request != nil
          return @cached_request
        end
        return @cached_request = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:request, []).call(),::Vertx::HttpServerRequest)
      end
      raise ArgumentError, "Invalid arguments when calling request()"
    end
    # @return [::Vertx::HttpServerResponse] the HTTP response object
    def response
      if !block_given?
        if @cached_response != nil
          return @cached_response
        end
        return @cached_response = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:response, []).call(),::Vertx::HttpServerResponse)
      end
      raise ArgumentError, "Invalid arguments when calling response()"
    end
    #  Tell the router to route this context to the next matching route (if any).
    #  This method, if called, does not need to be called during the execution of the handler, it can be called
    #  some arbitrary time later, if required.
    #  <p>
    #  If next is not called for a handler then the handler should make sure it ends the response or no response
    #  will be sent.
    # @return [void]
    def next
      if !block_given?
        return @j_del.java_method(:next, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling next()"
    end
    #  Fail the context with the specified throwable.
    #  <p>
    #  This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
    #  match a default failure response with status code 500 will be sent.
    # @overload fail(statusCode)
    #   @param [Fixnum] statusCode the HTTP status code
    # @overload fail(throwable)
    #   @param [Exception] throwable a throwable representing the failure
    # @return [void]
    def fail(param_1=nil)
      if param_1.class == Fixnum && !block_given?
        return @j_del.java_method(:fail, [Java::int.java_class]).call(param_1)
      elsif param_1.is_a?(Exception) && !block_given?
        return @j_del.java_method(:fail, [Java::JavaLang::Throwable.java_class]).call(::Vertx::Util::Utils.to_throwable(param_1))
      end
      raise ArgumentError, "Invalid arguments when calling fail(param_1)"
    end
    #  Put some arbitrary data in the context. This will be available in any handlers that receive the context.
    # @param [String] key the key for the data
    # @param [Object] obj the data
    # @return [self]
    def put(key=nil,obj=nil)
      if key.class == String && (obj.class == String  || obj.class == Hash || obj.class == Array || obj.class == NilClass || obj.class == TrueClass || obj.class == FalseClass || obj.class == Fixnum || obj.class == Float) && !block_given?
        @j_del.java_method(:put, [Java::java.lang.String.java_class,Java::java.lang.Object.java_class]).call(key,::Vertx::Util::Utils.to_object(obj))
        return self
      end
      raise ArgumentError, "Invalid arguments when calling put(key,obj)"
    end
    #  Get some data from the context. The data is available in any handlers that receive the context.
    # @param [String] key the key for the data
    # @return [Object] the data
    def get(key=nil)
      if key.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:get, [Java::java.lang.String.java_class]).call(key))
      end
      raise ArgumentError, "Invalid arguments when calling get(key)"
    end
    #  Remove some data from the context. The data is available in any handlers that receive the context.
    # @param [String] key the key for the data
    # @return [Object] the previous data associated with the key
    def remove(key=nil)
      if key.class == String && !block_given?
        return ::Vertx::Util::Utils.from_object(@j_del.java_method(:remove, [Java::java.lang.String.java_class]).call(key))
      end
      raise ArgumentError, "Invalid arguments when calling remove(key)"
    end
    # @return [::Vertx::Vertx] the Vert.x instance associated to the initiating {::VertxWeb::Router} for this context
    def vertx
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:vertx, []).call(),::Vertx::Vertx)
      end
      raise ArgumentError, "Invalid arguments when calling vertx()"
    end
    # @return [String] the mount point for this router. It will be null for a top level router. For a sub-router it will be the path at which the subrouter was mounted.
    def mount_point
      if !block_given?
        return @j_del.java_method(:mountPoint, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling mount_point()"
    end
    # @return [::VertxWeb::Route] the current route this context is being routed through.
    def current_route
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:currentRoute, []).call(),::VertxWeb::Route)
      end
      raise ArgumentError, "Invalid arguments when calling current_route()"
    end
    #  Return the normalised path for the request.
    #  <p>
    #  The normalised path is where the URI path has been decoded, i.e. any unicode or other illegal URL characters that
    #  were encoded in the original URL with `%` will be returned to their original form. E.g. `%20` will revert to a space.
    #  Also `+` reverts to a space in a query.
    #  <p>
    #  The normalised path will also not contain any `..` character sequences to prevent resources being accessed outside
    #  of the permitted area.
    #  <p>
    #  It's recommended to always use the normalised path as opposed to 
    #  if accessing server resources requested by a client.
    # @return [String] the normalised path
    def normalised_path
      if !block_given?
        return @j_del.java_method(:normalisedPath, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling normalised_path()"
    end
    #  Get the cookie with the specified name. The context must have first been routed to a {::VertxWeb::CookieHandler}
    #  for this to work.
    # @param [String] name the cookie name
    # @return [::VertxWeb::Cookie] the cookie
    def get_cookie(name=nil)
      if name.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getCookie, [Java::java.lang.String.java_class]).call(name),::VertxWeb::Cookie)
      end
      raise ArgumentError, "Invalid arguments when calling get_cookie(name)"
    end
    #  Add a cookie. This will be sent back to the client in the response. The context must have first been routed
    #  to a {::VertxWeb::CookieHandler} for this to work.
    # @param [::VertxWeb::Cookie] cookie the cookie
    # @return [self]
    def add_cookie(cookie=nil)
      if cookie.class.method_defined?(:j_del) && !block_given?
        @j_del.java_method(:addCookie, [Java::IoVertxExtWeb::Cookie.java_class]).call(cookie.j_del)
        return self
      end
      raise ArgumentError, "Invalid arguments when calling add_cookie(cookie)"
    end
    #  Remove a cookie. The context must have first been routed to a {::VertxWeb::CookieHandler}
    #  for this to work.
    # @param [String] name the name of the cookie
    # @return [::VertxWeb::Cookie] the cookie, if it existed, or null
    def remove_cookie(name=nil)
      if name.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:removeCookie, [Java::java.lang.String.java_class]).call(name),::VertxWeb::Cookie)
      end
      raise ArgumentError, "Invalid arguments when calling remove_cookie(name)"
    end
    # @return [Fixnum] the number of cookies. The context must have first been routed to a {::VertxWeb::CookieHandler} for this to work.
    def cookie_count
      if !block_given?
        return @j_del.java_method(:cookieCount, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling cookie_count()"
    end
    # @return [Set<::VertxWeb::Cookie>] a set of all the cookies. The context must have first been routed to a {::VertxWeb::CookieHandler} for this to be populated.
    def cookies
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:cookies, []).call()).map! { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::Cookie) }
      end
      raise ArgumentError, "Invalid arguments when calling cookies()"
    end
    #  Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
    #  {::VertxWeb::BodyHandler} for this to be populated.
    # @param [String] encoding the encoding, e.g. "UTF-16"
    # @return [String] the body
    def get_body_as_string(encoding=nil)
      if !block_given? && encoding == nil
        return @j_del.java_method(:getBodyAsString, []).call()
      elsif encoding.class == String && !block_given?
        return @j_del.java_method(:getBodyAsString, [Java::java.lang.String.java_class]).call(encoding)
      end
      raise ArgumentError, "Invalid arguments when calling get_body_as_string(encoding)"
    end
    # @return [Hash{String => Object}] Get the entire HTTP request body as a . The context must have first been routed to a {::VertxWeb::BodyHandler} for this to be populated.
    def get_body_as_json
      if !block_given?
        return @j_del.java_method(:getBodyAsJson, []).call() != nil ? JSON.parse(@j_del.java_method(:getBodyAsJson, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_body_as_json()"
    end
    # @return [Array<String,Object>] Get the entire HTTP request body as a . The context must have first been routed to a {::VertxWeb::BodyHandler} for this to be populated.
    def get_body_as_json_array
      if !block_given?
        return @j_del.java_method(:getBodyAsJsonArray, []).call() != nil ? JSON.parse(@j_del.java_method(:getBodyAsJsonArray, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_body_as_json_array()"
    end
    # @return [::Vertx::Buffer] Get the entire HTTP request body as a . The context must have first been routed to a {::VertxWeb::BodyHandler} for this to be populated.
    def get_body
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getBody, []).call(),::Vertx::Buffer)
      end
      raise ArgumentError, "Invalid arguments when calling get_body()"
    end
    # @return [Set<::VertxWeb::FileUpload>] a set of fileuploads (if any) for the request. The context must have first been routed to a {::VertxWeb::BodyHandler} for this to work.
    def file_uploads
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:fileUploads, []).call()).map! { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::FileUpload) }
      end
      raise ArgumentError, "Invalid arguments when calling file_uploads()"
    end
    #  Get the session. The context must have first been routed to a {::VertxWeb::SessionHandler}
    #  for this to be populated.
    #  Sessions live for a browser session, and are maintained by session cookies.
    # @return [::VertxWeb::Session] the session.
    def session
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:session, []).call(),::VertxWeb::Session)
      end
      raise ArgumentError, "Invalid arguments when calling session()"
    end
    #  Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.
    # @return [::VertxAuthCommon::User] the user, or null if the current user is not authenticated.
    def user
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:user, []).call(),::VertxAuthCommon::User)
      end
      raise ArgumentError, "Invalid arguments when calling user()"
    end
    #  If the context is being routed to failure handlers after a failure has been triggered by calling
    #  {::VertxWeb::RoutingContext#fail} then this will return that throwable. It can be used by failure handlers to render a response,
    #  e.g. create a failure response page.
    # @return [Exception] the throwable used when signalling failure
    def failure
      if !block_given?
        if @cached_failure != nil
          return @cached_failure
        end
        return @cached_failure = ::Vertx::Util::Utils.from_throwable(@j_del.java_method(:failure, []).call())
      end
      raise ArgumentError, "Invalid arguments when calling failure()"
    end
    #  If the context is being routed to failure handlers after a failure has been triggered by calling
    #  {::VertxWeb::RoutingContext#fail}  then this will return that status code.  It can be used by failure handlers to render a response,
    #  e.g. create a failure response page.
    # 
    #  When the status code has not been set yet (it is undefined) its value will be -1.
    # @return [Fixnum] the status code used when signalling failure
    def status_code
      if !block_given?
        if @cached_status_code != nil
          return @cached_status_code
        end
        return @cached_status_code = @j_del.java_method(:statusCode, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling status_code()"
    end
    #  If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
    #  matches one or more of these then this returns the most acceptable match.
    # @return [String] the most acceptable content type.
    def get_acceptable_content_type
      if !block_given?
        return @j_del.java_method(:getAcceptableContentType, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling get_acceptable_content_type()"
    end
    #  The headers:
    #  <ol>
    #  <li>Accept</li>
    #  <li>Accept-Charset</li>
    #  <li>Accept-Encoding</li>
    #  <li>Accept-Language</li>
    #  <li>Content-Type</li>
    #  </ol>
    #  Parsed into {::VertxWeb::ParsedHeaderValue}
    # @return [::VertxWeb::ParsedHeaderValues] A container with the parsed headers.
    def parsed_headers
      if !block_given?
        if @cached_parsed_headers != nil
          return @cached_parsed_headers
        end
        return @cached_parsed_headers = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:parsedHeaders, []).call(),::VertxWeb::ParsedHeaderValues)
      end
      raise ArgumentError, "Invalid arguments when calling parsed_headers()"
    end
    #  Add a handler that will be called just before headers are written to the response. This gives you a hook where
    #  you can write any extra headers before the response has been written when it will be too late.
    # @yield the handler
    # @return [Fixnum] the id of the handler. This can be used if you later want to remove the handler.
    def add_headers_end_handler
      if block_given?
        return @j_del.java_method(:addHeadersEndHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
      end
      raise ArgumentError, "Invalid arguments when calling add_headers_end_handler()"
    end
    #  Remove a headers end handler
    # @param [Fixnum] handlerID the id as returned from {::VertxWeb::RoutingContext#add_headers_end_handler}.
    # @return [true,false] true if the handler existed and was removed, false otherwise
    def remove_headers_end_handler?(handlerID=nil)
      if handlerID.class == Fixnum && !block_given?
        return @j_del.java_method(:removeHeadersEndHandler, [Java::int.java_class]).call(handlerID)
      end
      raise ArgumentError, "Invalid arguments when calling remove_headers_end_handler?(handlerID)"
    end
    #  Provides a handler that will be called after the last part of the body is written to the wire.
    #  The handler is called asynchronously of when the response has been received by the client.
    #  This provides a hook allowing you to do more operations once the request has been sent over the wire
    #  such as resource cleanup.
    # @yield the handler
    # @return [Fixnum] the id of the handler. This can be used if you later want to remove the handler.
    def add_body_end_handler
      if block_given?
        return @j_del.java_method(:addBodyEndHandler, [Java::IoVertxCore::Handler.java_class]).call(Proc.new { yield })
      end
      raise ArgumentError, "Invalid arguments when calling add_body_end_handler()"
    end
    #  Remove a body end handler
    # @param [Fixnum] handlerID the id as returned from {::VertxWeb::RoutingContext#add_body_end_handler}.
    # @return [true,false] true if the handler existed and was removed, false otherwise
    def remove_body_end_handler?(handlerID=nil)
      if handlerID.class == Fixnum && !block_given?
        return @j_del.java_method(:removeBodyEndHandler, [Java::int.java_class]).call(handlerID)
      end
      raise ArgumentError, "Invalid arguments when calling remove_body_end_handler?(handlerID)"
    end
    # @return [true,false] true if the context is being routed to failure handlers.
    def failed?
      if !block_given?
        return @j_del.java_method(:failed, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling failed?()"
    end
    #  Set the body. Used by the {::VertxWeb::BodyHandler}. You will not normally call this method.
    # @param [::Vertx::Buffer] body the body
    # @return [void]
    def set_body(body=nil)
      if body.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setBody, [Java::IoVertxCoreBuffer::Buffer.java_class]).call(body.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_body(body)"
    end
    #  Set the session. Used by the {::VertxWeb::SessionHandler}. You will not normally call this method.
    # @param [::VertxWeb::Session] session the session
    # @return [void]
    def set_session(session=nil)
      if session.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setSession, [Java::IoVertxExtWeb::Session.java_class]).call(session.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_session(session)"
    end
    #  Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
    # @param [::VertxAuthCommon::User] user the user
    # @return [void]
    def set_user(user=nil)
      if user.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:setUser, [Java::IoVertxExtAuth::User.java_class]).call(user.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling set_user(user)"
    end
    #  Clear the current user object in the context. This usually is used for implementing a log out feature, since the
    #  current user is unbounded from the routing context.
    # @return [void]
    def clear_user
      if !block_given?
        return @j_del.java_method(:clearUser, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling clear_user()"
    end
    #  Set the acceptable content type. Used by
    # @param [String] contentType the content type
    # @return [void]
    def set_acceptable_content_type(contentType=nil)
      if contentType.class == String && !block_given?
        return @j_del.java_method(:setAcceptableContentType, [Java::java.lang.String.java_class]).call(contentType)
      end
      raise ArgumentError, "Invalid arguments when calling set_acceptable_content_type(contentType)"
    end
    #  Restarts the current router with a new method and path. All path parameters are then parsed and available on the
    #  params list.
    # @overload reroute(path)
    #   @param [String] path the new http path.
    # @overload reroute(method,path)
    #   @param [:OPTIONS,:GET,:HEAD,:POST,:PUT,:DELETE,:TRACE,:CONNECT,:PATCH,:OTHER] method the new http request
    #   @param [String] path the new http path.
    # @return [void]
    def reroute(param_1=nil,param_2=nil)
      if param_1.class == String && !block_given? && param_2 == nil
        return @j_del.java_method(:reroute, [Java::java.lang.String.java_class]).call(param_1)
      elsif param_1.class == Symbol && param_2.class == String && !block_given?
        return @j_del.java_method(:reroute, [Java::IoVertxCoreHttp::HttpMethod.java_class,Java::java.lang.String.java_class]).call(Java::IoVertxCoreHttp::HttpMethod.valueOf(param_1),param_2)
      end
      raise ArgumentError, "Invalid arguments when calling reroute(param_1,param_2)"
    end
    #  Returns the locales for the current request. The locales are determined from the `accept-languages` header and
    #  sorted on quality.
    # 
    #  When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
    #  index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
    #  match will be en-US because it was declared as first entry by the client.
    # @return [Array<::VertxWeb::Locale>] the best matched locale for the request
    def acceptable_locales
      if !block_given?
        if @cached_acceptable_locales != nil
          return @cached_acceptable_locales
        end
        return @cached_acceptable_locales = @j_del.java_method(:acceptableLocales, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::Locale) }
      end
      raise ArgumentError, "Invalid arguments when calling acceptable_locales()"
    end
    #  Returns the languages for the current request. The languages are determined from the <code>Accept-Language</code>
    #  header and sorted on quality.
    # 
    #  When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
    #  index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
    #  match will be en-US because it was declared as first entry by the client.
    # @return [Array<::VertxWeb::LanguageHeader>] The best matched language for the request
    def acceptable_languages
      if !block_given?
        if @cached_acceptable_languages != nil
          return @cached_acceptable_languages
        end
        return @cached_acceptable_languages = @j_del.java_method(:acceptableLanguages, []).call().to_a.map { |elt| ::Vertx::Util::Utils.safe_create(elt,::VertxWeb::LanguageHeader) }
      end
      raise ArgumentError, "Invalid arguments when calling acceptable_languages()"
    end
    #  Helper to return the user preferred locale. It is the same action as returning the first element of the acceptable
    #  locales.
    # @return [::VertxWeb::Locale] the users preferred locale.
    def preferred_locale
      if !block_given?
        if @cached_preferred_locale != nil
          return @cached_preferred_locale
        end
        return @cached_preferred_locale = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:preferredLocale, []).call(),::VertxWeb::Locale)
      end
      raise ArgumentError, "Invalid arguments when calling preferred_locale()"
    end
    #  Helper to return the user preferred language.
    #  It is the same action as returning the first element of the acceptable languages.
    # @return [::VertxWeb::LanguageHeader] the users preferred locale.
    def preferred_language
      if !block_given?
        if @cached_preferred_language != nil
          return @cached_preferred_language
        end
        return @cached_preferred_language = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:preferredLanguage, []).call(),::VertxWeb::LanguageHeader)
      end
      raise ArgumentError, "Invalid arguments when calling preferred_language()"
    end
    #  Returns a map of named parameters as defined in path declaration with their actual values
    # @return [Hash{String => String}] the map of named parameters
    def path_params
      if !block_given?
        return Java::IoVertxLangRuby::Helper.adaptingMap(@j_del.java_method(:pathParams, []).call(), Proc.new { |val| ::Vertx::Util::Utils.from_object(val) }, Proc.new { |val| ::Vertx::Util::Utils.to_string(val) })
      end
      raise ArgumentError, "Invalid arguments when calling path_params()"
    end
    #  Gets the value of a single path parameter
    # @param [String] name the name of parameter as defined in path declaration
    # @return [String] the actual value of the parameter or null if it doesn't exist
    def path_param(name=nil)
      if name.class == String && !block_given?
        return @j_del.java_method(:pathParam, [Java::java.lang.String.java_class]).call(name)
      end
      raise ArgumentError, "Invalid arguments when calling path_param(name)"
    end
  end
end
