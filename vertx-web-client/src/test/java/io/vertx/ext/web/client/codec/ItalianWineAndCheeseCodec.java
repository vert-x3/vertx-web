package io.vertx.ext.web.client.codec;

import io.vertx.core.spi.json.JsonCodec;
import io.vertx.core.json.JsonObject;

public class ItalianWineAndCheeseCodec implements JsonCodec<ItalianWineAndCheese, JsonObject> {

  @Override
  public ItalianWineAndCheese decode(JsonObject value) throws IllegalArgumentException {
    return new ItalianWineAndCheese()
      .setCheese(value.getString("cheese"))
      .setWine("Trebbiano D'Abruzzo");
  }

  @Override
  public JsonObject encode(ItalianWineAndCheese value) throws IllegalArgumentException {
    return new JsonObject()
      .put("cheese", value.getCheese())
      .put("wine", "Trebbiano D'Abruzzo");
  }

  @Override
  public Class<ItalianWineAndCheese> getTargetClass() {
    return ItalianWineAndCheese.class;
  }
}
