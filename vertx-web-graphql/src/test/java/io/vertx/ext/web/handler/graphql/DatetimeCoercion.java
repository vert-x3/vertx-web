package io.vertx.ext.web.handler.graphql;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class DatetimeCoercion implements Coercing<LocalDateTime, String> {

  DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

  @Override
  public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
    if (dataFetcherResult == null)
      return null;
    if (!(dataFetcherResult instanceof LocalDateTime))
      throw new CoercingSerializeException(dataFetcherResult.getClass().getCanonicalName() + "is not a date!");
    LocalDateTime localDateTime = (LocalDateTime) dataFetcherResult;
    return dateTimeFormatter.format(localDateTime);
  }

  @Override
  public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
    if (input == null)
      return null;

    String source = input.toString();
    try {
      final TemporalAccessor temporalAccessorParsed = dateTimeFormatter.parse(source);
      return LocalDateTime.from(temporalAccessorParsed);
    } catch (DateTimeParseException dateTimeParseException) {
      throw new CoercingParseValueException(dateTimeParseException);
    }
  }

  @Override
  public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
    return parseValue(input);
  }
}
