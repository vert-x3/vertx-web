package io.vertx.ext.web.handler.graphql;

public class Person {
  public String name;
  private Person friend;

  public Person(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setFriend(Person friend) {
    this.friend = friend;
  }

  public Person getFriend() {
    return friend;
  }
}
