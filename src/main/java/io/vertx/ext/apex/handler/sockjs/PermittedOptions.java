package io.vertx.ext.apex.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Specify a match to allow for inbound and outbound traffic using the
 * {@link io.vertx.ext.apex.handler.sockjs.BridgeOptions}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class PermittedOptions {

  /**
   * The default permission address : {@code null}.
   */
  public static String DEFAULT_ADDRESS = null;

  /**
   * The default permission address regex : {@code null}.
   */
  public static String DEFAULT_ADDRESS_REGEX = null;

  /**
   * The default permission required role : {@code null}.
   */
  public static String DEFAULT_REQUIRED_ROLE = null;

  /**
   * The default permission required permission : {@code null}.
   */
  public static String DEFAULT_REQUIRED_PERMISSION = null;

  /**
   * The default permission match : {@code null}.
   */
  public static JsonObject DEFAULT_MATCH = null;

  private String address;
  private String addressRegex;
  private String requiredRole;
  private String requiredPermission;
  private JsonObject match;

  public PermittedOptions() {
  }

  public PermittedOptions(PermittedOptions that) {
    address = that.address;
    addressRegex = that.addressRegex;
    requiredRole = that.requiredRole;
    requiredPermission = that.requiredPermission;
    match = that.match != null ? new JsonObject(that.match.encode()) : null;
  }

  public PermittedOptions(JsonObject json) {
    address = json.getString("address", DEFAULT_ADDRESS);
    addressRegex = json.getString("addressRegex", DEFAULT_ADDRESS_REGEX);
    requiredRole = json.getString("requiredRole", DEFAULT_REQUIRED_ROLE);
    requiredPermission = json.getString("requiredPermission", DEFAULT_REQUIRED_PERMISSION);
    match = json.getJsonObject("match", DEFAULT_MATCH);
  }

  public String getAddress() {
    return address;
  }

  /**
   * The exact address the message is being sent to. If you want to allow messages based on
   * an exact address you use this field.
   *
   * @param address the address
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setAddress(String address) {
    this.address = address;
    return this;
  }

  public String getAddressRegex() {
    return addressRegex;
  }

  /**
   * A regular expression that will be matched against the address. If you want to allow messages
   * based on a regular expression you use this field. If the {@link #setAddress} value is specified
   * this will be ignored.
   *
   * @param addressRegex the address regex
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setAddressRegex(String addressRegex) {
    this.addressRegex = addressRegex;
    return this;
  }

  public String getRequiredRole() {
    return requiredRole;
  }

  /**
   * Declare a specific role for the logged-in user is required in order to access allow the messages.
   *
   * @param requiredRole the role
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setRequiredRole(String requiredRole) {
    this.requiredRole = requiredRole;
    return this;
  }

  public String getRequiredPermission() {
    return requiredPermission;
  }

  /**
   * Declare a specific permission for the logged-in user is required in order to access allow the messages;
   *
   * @param requiredPermission the permission
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setRequiredPermission(String requiredPermission) {
    this.requiredPermission = requiredPermission;
    return this;
  }

  public JsonObject getMatch() {
    return match;
  }

  /**
   * This allows you to allow messages based on their structure. Any fields in the match must exist in the
   * message with the same values for them to be allowed. This currently only works with JSON messages.
   *
   * @param match the match json object
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setMatch(JsonObject match) {
    this.match = match;
    return this;
  }
}
