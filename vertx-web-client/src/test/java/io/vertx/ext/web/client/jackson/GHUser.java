package io.vertx.ext.web.client.jackson;

public class GHUser {

  private String login;
  private long id;

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
