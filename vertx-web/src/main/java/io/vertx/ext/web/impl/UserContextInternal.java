package io.vertx.ext.web.impl;

import io.vertx.ext.auth.User;
import io.vertx.ext.web.UserContext;

public interface UserContextInternal extends UserContext {

  /**
   * Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
   *
   * @param user  the user
   */
  void setUser(User user);
}
