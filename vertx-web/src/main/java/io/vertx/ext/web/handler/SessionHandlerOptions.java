/*
 * Copyright 2024 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.json.JsonObject;

/**
 * Options for the {@link SessionHandler}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class SessionHandlerOptions {

  /**
   * Default name of session cookie.
   */
  public static final String DEFAULT_SESSION_COOKIE_NAME = "vertx-web.session";

  /**
   * Default path of session cookie.
   */
  public static final String DEFAULT_SESSION_COOKIE_PATH = "/";

  /**
   * Default time, in ms, that a session lasts for without being accessed before expiring.
   */
  public static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

  /**
   * Default of whether a nagging log warning should be written if the session handler is accessed over HTTP, not HTTPS.
   */
  public static final boolean DEFAULT_NAG_HTTPS = true;

  /**
   * Default of whether the cookie has the HttpOnly flag set.
   * <p>
   * <a href="https://www.owasp.org/index.php/HttpOnly">More info</a>.
   */
  public static final boolean DEFAULT_COOKIE_HTTP_ONLY_FLAG = false;

  /**
   * Default of whether the cookie has the 'secure' flag set to allow transmission over https only.
   * <p>
   * <a href="https://www.owasp.org/index.php/SecureFlag">More info</a>.
   */
  public static final boolean DEFAULT_COOKIE_SECURE_FLAG = false;

  /**
   * Default min length for a session id.
   * <p>
   * <a href="https://www.owasp.org/index.php/Session_Management_Cheat_Sheet">More info</a>.
   */
  public static final int DEFAULT_SESSIONID_MIN_LENGTH = 16;

  /**
   * Default of whether the session should be created lazily.
   */
  public static final boolean DEFAULT_LAZY_SESSION = false;

  private long sessionTimeout;
  private boolean nagHttps;
  private boolean cookieSecureFlag;
  private boolean cookieHttpOnlyFlag;
  private String sessionCookieName;
  private String sessionCookiePath;
  private int minLength;
  private CookieSameSite cookieSameSite;
  private boolean lazySession;
  private long cookieMaxAge;
  private boolean cookieless;
  private String signingSecret;


  /**
   * Default constructor.
   */
  public SessionHandlerOptions() {
    sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    nagHttps = DEFAULT_NAG_HTTPS;
    cookieSecureFlag = DEFAULT_COOKIE_SECURE_FLAG;
    cookieHttpOnlyFlag = DEFAULT_COOKIE_HTTP_ONLY_FLAG;
    sessionCookieName = DEFAULT_SESSION_COOKIE_NAME;
    sessionCookiePath = DEFAULT_SESSION_COOKIE_PATH;
    minLength = DEFAULT_SESSIONID_MIN_LENGTH;
    lazySession = DEFAULT_LAZY_SESSION;
    cookieMaxAge = -1;
    cookieless = false;
    signingSecret = null;
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public SessionHandlerOptions(SessionHandlerOptions other) {
    this();
    sessionTimeout = other.sessionTimeout;
    nagHttps = other.nagHttps;
    cookieSecureFlag = other.cookieSecureFlag;
    cookieHttpOnlyFlag = other.cookieHttpOnlyFlag;
    sessionCookieName = other.sessionCookieName;
    sessionCookiePath = other.sessionCookiePath;
    minLength = other.minLength;
    cookieSameSite = other.cookieSameSite;
    lazySession = other.lazySession;
    cookieMaxAge = other.cookieMaxAge;
    cookieless = other.cookieless;
    signingSecret = other.signingSecret;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public SessionHandlerOptions(JsonObject json) {
    this();
    SessionHandlerOptionsConverter.fromJson(json, this);
  }


  /**
   * Set the session timeout.
   *
   * @param sessionTimeout the timeout, in ms.
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setSessionTimeout(long sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
    return this;
  }

  /**
   * Set whether a nagging log warning should be written if the session handler is accessed over HTTP, not HTTPS.
   *
   * @param nagHttps true to nag
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setNagHttps(boolean nagHttps) {
    this.nagHttps = nagHttps;
    return this;
  }

  /**
   * Sets whether the 'secure' flag should be set for the session cookie.
   * <p>
   * When set, this flag instructs browsers to only send the cookie over HTTPS.
   * Note that this will probably stop your sessions working if used without HTTPS (e.g. in development).
   *
   * @param cookieSecureFlag true to set the secure flag on the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setCookieSecureFlag(boolean cookieSecureFlag) {
    this.cookieSecureFlag = cookieSecureFlag;
    return this;
  }

  /**
   * Sets whether the 'HttpOnly' flag should be set for the session cookie.
   * <p>
   * When set, this flag instructs browsers to prevent Javascript access to the cookie.
   * Used as a line of defence against the most common XSS attacks.
   *
   * @param cookieHttpOnlyFlag true to set the HttpOnly flag on the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setCookieHttpOnlyFlag(boolean cookieHttpOnlyFlag) {
    this.cookieHttpOnlyFlag = cookieHttpOnlyFlag;
    return this;
  }

  /**
   * Set the session cookie name.
   *
   * @param sessionCookieName the session cookie name
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setSessionCookieName(String sessionCookieName) {
    this.sessionCookieName = sessionCookieName;
    return this;
  }

  /**
   * Set the session cookie path.
   *
   * @param sessionCookiePath the session cookie path
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setSessionCookiePath(String sessionCookiePath) {
    this.sessionCookiePath = sessionCookiePath;
    return this;
  }

  /**
   * Set expected session id minimum length.
   *
   * @param minLength the session id minimal length
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setMinLength(int minLength) {
    this.minLength = minLength;
    return this;
  }

  /**
   * Set the session cookie {@link CookieSameSite} policy to use.
   *
   * @param cookieSameSite to use, {@code null} for no policy.
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setCookieSameSite(CookieSameSite cookieSameSite) {
    this.cookieSameSite = cookieSameSite;
    return this;
  }

  /**
   * Use a lazy session creation mechanism.
   * <p>
   * The session will only be created when accessed from the context.
   * Thus, the session cookie is set only if the session was accessed.
   *
   * @param lazySession true to have a lazy session creation.
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setLazySession(boolean lazySession) {
    this.lazySession = lazySession;
    return this;
  }

  /**
   * Set a Cookie max-age to the session cookie.
   * <p>
   * When doing this the Cookie will be persistent across browser restarts.
   * This can be dangerous as closing a browser windows does not invalidate the session.
   * For more information refer to OWASP's <a href="https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html#Expire_and_Max-Age_Attributes">Session Management Cheat Sheet</a>
   *
   * @param cookieMaxAge a non-negative max-age, note that 0 means expire now.
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setCookieMaxAge(long cookieMaxAge) {
    this.cookieMaxAge = cookieMaxAge;
    return this;
  }

  /**
   * Use sessions based on url paths instead of cookies.
   * <p>
   * This is a potential less safe alternative to cookies but offers an alternative when Cookies are not desired.
   * For example, to avoid showing banners on a website due to cookie laws, or doing machine to machine operations where state is required to maintain.
   *
   * @param cookieless true if a cookieless session should be used
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setCookieless(boolean cookieless) {
    this.cookieless = cookieless;
    return this;
  }

  /**
   * Set signing secret for the session cookie.
   * <p>
   * The cookie will not be signed and verified by the {@link SessionHandler} if this is not set.
   * But it may be signed by the session implementation, for example, CookieSessionStore signs the cookie data.
   *
   * @param signingSecret the secret used to sign the session cookie data
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandlerOptions setSigningSecret(String signingSecret) {
    this.signingSecret = signingSecret;
    return this;
  }

  public long getSessionTimeout() {
    return sessionTimeout;
  }

  public boolean isNagHttps() {
    return nagHttps;
  }

  public boolean isCookieSecureFlag() {
    return cookieSecureFlag;
  }

  public boolean isCookieHttpOnlyFlag() {
    return cookieHttpOnlyFlag;
  }

  public String getSessionCookieName() {
    return sessionCookieName;
  }

  public String getSessionCookiePath() {
    return sessionCookiePath;
  }

  public int getMinLength() {
    return minLength;
  }

  public CookieSameSite getCookieSameSite() {
    return cookieSameSite;
  }

  public boolean isLazySession() {
    return lazySession;
  }

  public long getCookieMaxAge() {
    return cookieMaxAge;
  }

  public boolean isCookieless() {
    return cookieless;
  }

  public String getSigningSecret() {
    return signingSecret;
  }
}
