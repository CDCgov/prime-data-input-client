package gov.cdc.usds.simplereport.api;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class FlexibleDateCoercion implements Coercing<Object, Object> {
  private static final DateTimeFormatter US_DASHDATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter US_SLASHDATE_FORMATTER =
      DateTimeFormatter.ofPattern("M/d/yyyy");
  private static final DateTimeFormatter US_SLASHDATE_TWO_DIGIT_YEAR_FORMATTER =
      DateTimeFormatter.ofPattern("M/d/yy");
  private static int CENTURY = 100;

  LocalDate convertImpl(Object input) {
    if (input instanceof String) {
      if (((String) input).contains("/")) {
        String[] dateParts = ((String) input).split("/");
        if (dateParts[2].length() == 2) {
          LocalDate date = LocalDate.parse((String) input, US_SLASHDATE_TWO_DIGIT_YEAR_FORMATTER);
          return date.isBefore(LocalDate.now()) ? date : date.minus(Period.ofYears(CENTURY));
        }
        return LocalDate.parse((String) input, US_SLASHDATE_FORMATTER);
      } else if (((String) input).contains("-")) {
        return LocalDate.parse((String) input, US_DASHDATE_FORMATTER);
      }
    } else if (input instanceof LocalDate) {
      return (LocalDate) input;
    }
    return null;
  }

  @Override
  public Object serialize(Object dataFetcherResult) {
    LocalDate result = convertImpl(dataFetcherResult);
    if (result == null) {
      throw new CoercingSerializeException("Unable to serialize null value");
    }
    return result.format(US_DASHDATE_FORMATTER);
  }

  @Override
  public Object parseValue(Object input) {
    if (((String) input).length() == 0) {
      return null;
    }
    LocalDate result = convertImpl(input);
    if (result == null) {
      throw new CoercingParseValueException("Invalid value '" + input + "' for LocalDate");
    }
    return result;
  }

  @Override
  public Object parseLiteral(Object input) {
    String value = ((StringValue) input).getValue();
    LocalDate result = convertImpl(value);
    if (result == null) {
      throw new CoercingParseLiteralException("Invalid value '" + input + "' for LocalDate");
    }

    return result;
  }
}

@Configuration
public class FlexibleDateScalarType {
  @Bean
  public GraphQLScalarType FlexibleDateScalar() {
    return GraphQLScalarType.newScalar()
        .name("LocalDate")
        .description("a scalar for multiple date formats. currently yyyy-MM-dd and MM/dd/yyyy")
        .coercing(new FlexibleDateCoercion())
        .build();
  }
}
