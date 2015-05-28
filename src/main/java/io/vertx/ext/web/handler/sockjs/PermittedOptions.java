package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Specify a match to allow for inbound and outbound traffic using the
 * {@link io.vertx.ext.web.handler.sockjs.BridgeOptions}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class PermittedOptions {

  /**
   * The default permitted address : {@code null}.
   */
  public static String DEFAULT_ADDRESS = null;

  /**
   * The default permitted address regex : {@code null}.
   */
  public static String DEFAULT_ADDRESS_REGEX = null;

  /**
   * The default permitted required authority : {@code null}.
   */
  public static String DEFAULT_REQUIRED_AUTHORITY = null;

  /**
   * The default permitted match : {@code null}.
   */
  public static JsonObject DEFAULT_MATCH = null;

  private String address;
  private String addressRegex;
  private String requiredAuthority;

  private JsonObject match;

  public PermittedOptions() {
  }

  public PermittedOptions(PermittedOptions that) {
    address = that.address;
    addressRegex = that.addressRegex;
    requiredAuthority = that.requiredAuthority;
    match = that.match != null ? new JsonObject(that.match.encode()) : null;
  }

  public PermittedOptions(JsonObject json) {
    address = json.getString("address", DEFAULT_ADDRESS);
    addressRegex = json.getString("addressRegex", DEFAULT_ADDRESS_REGEX);
    requiredAuthority = json.getString("requiredAuthority", DEFAULT_REQUIRED_AUTHORITY);
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

  public String getRequiredAuthority() {
    return requiredAuthority;
  }

  /**
   * Declare a specific authority that user must have in order to allow messages
   *
   * @param requiredAuthority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setRequiredAuthority(String requiredAuthority) {
    this.requiredAuthority = requiredAuthority;
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
