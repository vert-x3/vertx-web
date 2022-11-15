package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.impl.UserSwitchHandlerImpl;

@VertxGen
public interface UserSwitchHandler extends PlatformHandler {

  String USER_SWITCH_KEY = "__vertx.user-switch-ref";

  static UserSwitchHandler impersonate() {
    return new UserSwitchHandlerImpl(UserSwitchHandlerImpl.Mode.IMPERSONATE);
  }

  static UserSwitchHandler undo() {
    return new UserSwitchHandlerImpl(UserSwitchHandlerImpl.Mode.UNDO);
  }

  static UserSwitchHandler refresh() {
    return new UserSwitchHandlerImpl(UserSwitchHandlerImpl.Mode.REFRESH);
  }
}
