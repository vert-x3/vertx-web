package io.vertx.ext.healthchecks;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Represents the outcome of a health check procedure. Each procedure produces a {@link Status} indicating either OK
 * or KO. Optionally, it can also provide additional data.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class Status {

  /**
   * Creates a status when everything is fine.
   *
   * @return the created {@link Status}
   */
  public static Status OK() {
    return new Status().setOk(true);
  }

  /**
   * Creates a status when everything is fine and adds metadata.
   *
   * @return the created {@link Status}
   */
  public static Status OK(JsonObject data) {
    return new Status().setOk(true).setData(data);
  }

  /**
   * Creates a status when something bad is detected.
   *
   * @return the created {@link Status}
   */
  public static Status KO() {
    return new Status().setOk(false);
  }


  /**
   * Creates a status when something bad is detected. Also add some metadata.
   *
   * @return the created {@link Status}
   */
  public static Status KO(JsonObject data) {
    return new Status().setOk(false).setData(data);
  }

  /**
   * Whether or not the check is positive or negative.
   */
  private boolean ok;

  /**
   * Optional metadata attached to the status.
   */
  private JsonObject data = new JsonObject();

  /**
   * Flag denoting a failure, such as a timeout or a procedure throwing an exception.
   */
  private boolean procedureInError;

  /**
   * Creates a new instance of {@link Status} with default values.
   */
  public Status() {
    // Empty constructor
  }

  /**
   * Creates a new instance of {@link Status} by copying the given {@link Status}.
   *
   * @param other the status to copy, must not be {@code null}
   */
  public Status(Status other) {
    this.ok = other.ok;
    this.data = other.data;
    this.procedureInError = other.procedureInError;
  }

  /**
   * Creates a new instance of {@link Status} from the given JSON structure.
   *
   * @param json the serialized form, must not be {@code null}
   */
  public Status(JsonObject json) {
    StatusConverter.fromJson(json, this);
  }

  /**
   * Builds the JSON representation of the current {@link Status} instance.
   *
   * @return the json object
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    StatusConverter.toJson(this, json);
    return json;
  }

  /**
   * @return whether or not the current status is positive or negative.
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * Sets whether or not the current status is positive (UP) or negative (DOWN).
   *
   * @param ok {@code true} for UP, {@code false} for DOWN
   * @return the current status
   */
  public Status setOk(boolean ok) {
    this.ok = ok;
    return this;
  }

  /**
   * Sets the outcome of the status to KO.
   *
   * @return the current status
   */
  public Status setKO() {
    return this.setOk(false);
  }

  /**
   * Sets the outcome of the status to OK.
   *
   * @return the current status
   */
  public Status setOK() {
    return this.setOk(true);
  }

  /**
   * @return the additional metadata.
   */
  public JsonObject getData() {
    return data;
  }

  /**
   * Sets the metadata.
   *
   * @param data the data
   * @return the current status
   */
  public Status setData(JsonObject data) {
    this.data = data;
    return this;
  }

  /**
   * @return whether or not the status denotes a failure of a procedure.
   */
  public boolean isProcedureInError() {
    return procedureInError;
  }

  /**
   * Sets whether or not the procedure attached to this status has failed (timeout, error...).
   *
   * @param procedureInError {@code true} if the procedure has not been completed correctly.
   * @return the current status
   */
  public Status setProcedureInError(boolean procedureInError) {
    this.procedureInError = procedureInError;
    return this;
  }
}
