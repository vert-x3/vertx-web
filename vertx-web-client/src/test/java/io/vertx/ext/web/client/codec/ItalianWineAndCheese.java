package io.vertx.ext.web.client.codec;

import java.util.Objects;

public class ItalianWineAndCheese {

  private String wine;
  private String cheese;

  public String getWine() {
    return wine;
  }

  public ItalianWineAndCheese setWine(String wine) {
    this.wine = wine;
    return this;
  }

  public String getCheese() {
    return cheese;
  }

  public ItalianWineAndCheese setCheese(String cheese) {
    this.cheese = cheese;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ItalianWineAndCheese that = (ItalianWineAndCheese) o;
    return Objects.equals(wine, that.wine) &&
      Objects.equals(cheese, that.cheese);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wine, cheese);
  }
}
