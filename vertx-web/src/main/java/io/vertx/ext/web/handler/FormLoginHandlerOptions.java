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
import io.vertx.core.json.JsonObject;

/**
 * Options for the {@link io.vertx.ext.web.handler.FormLoginHandler}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class FormLoginHandlerOptions {

  /**
   * The default value of the form attribute which will contain the username.
   */
  public static final String DEFAULT_USERNAME_PARAM = "username";

  /**
   * The default value of the form attribute which will contain the password.
   */
  public static final String DEFAULT_PASSWORD_PARAM = "password";

  /**
   * The default value of the session attribute which will contain the return url.
   */
  public static final String DEFAULT_RETURN_URL_PARAM = "return_url";

  private String usernameParam;
  private String passwordParam;
  private String returnURLParam;
  private String directLoggedInOKURL;

  /**
   * Default constructor.
   */
  public FormLoginHandlerOptions() {
    usernameParam = DEFAULT_USERNAME_PARAM;
    passwordParam = DEFAULT_PASSWORD_PARAM;
    returnURLParam = DEFAULT_RETURN_URL_PARAM;
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public FormLoginHandlerOptions(FormLoginHandlerOptions other) {
    this();
    usernameParam = other.usernameParam;
    passwordParam = other.passwordParam;
    returnURLParam = other.returnURLParam;
    directLoggedInOKURL = other.directLoggedInOKURL;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public FormLoginHandlerOptions(JsonObject json) {
    this();
    FormLoginHandlerOptionsConverter.fromJson(json, this);
  }

  public String getUsernameParam() {
    return usernameParam;
  }

  /**
   * Set the name of the form param used to submit the username.
   *
   * @param usernameParam the name of the param
   * @return a reference to this, so the API can be used fluently
   */
  public FormLoginHandlerOptions setUsernameParam(String usernameParam) {
    this.usernameParam = usernameParam;
    return this;
  }

  public String getPasswordParam() {
    return passwordParam;
  }

  /**
   * Set the name of the form param used to submit the password.
   *
   * @param passwordParam the name of the param
   * @return a reference to this, so the API can be used fluently
   */
  public FormLoginHandlerOptions setPasswordParam(String passwordParam) {
    this.passwordParam = passwordParam;
    return this;
  }

  public String getReturnURLParam() {
    return returnURLParam;
  }

  /**
   * Set the name of the session attribute used to specify the return url.
   *
   * @param returnURLParam the name of the param
   * @return a reference to this, so the API can be used fluently
   */
  public FormLoginHandlerOptions setReturnURLParam(String returnURLParam) {
    this.returnURLParam = returnURLParam;
    return this;
  }

  public String getDirectLoggedInOKURL() {
    return directLoggedInOKURL;
  }

  /**
   * Set the url to redirect to if the user logs in directly at the url of the form login handler without being redirected here first.
   *
   * @param directLoggedInOKURL the URL to redirect to
   * @return a reference to this, so the API can be used fluently
   */
  public FormLoginHandlerOptions setDirectLoggedInOKURL(String directLoggedInOKURL) {
    this.directLoggedInOKURL = directLoggedInOKURL;
    return this;
  }
}
