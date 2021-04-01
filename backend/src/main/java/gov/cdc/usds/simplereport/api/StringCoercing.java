package gov.cdc.usds.simplereport.api;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

abstract class StringCoercing implements Coercing<String, String> {
  private final String typeName;

  protected StringCoercing(String typeName) {
    this.typeName = Objects.requireNonNull(typeName);
  }

  @Override
  public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
    return dataFetcherResult.toString();
  }

  @Override
  public String parseValue(Object input) throws CoercingParseValueException {
    try {
      return normalize(input.toString());
    } catch (IllegalGraphqlArgumentException e) {
      var builder = CoercingParseValueException.newCoercingParseValueException();
      buildException(e, builder::message, builder::sourceLocations, builder::path);
      throw builder.build();
    }
  }

  @Override
  public String parseLiteral(Object input) throws CoercingParseLiteralException {
    try {
      if (input instanceof StringValue) {
        return normalize(((StringValue) input).getValue());
      } else {
        throw new IllegalGraphqlArgumentException(
            typeName + " values must be serialized as strings.");
      }
    } catch (IllegalGraphqlArgumentException e) {
      var builder = CoercingParseLiteralException.newCoercingParseLiteralException();
      buildException(e, builder::message, builder::sourceLocations, builder::path);
      throw builder.build();
    }
  }

  /**
   * Normalizes the supplied string as required for the given data type.
   *
   * @throws IllegalGraphqlArgumentException when normalization fails.
   * @param inputValue The raw value supplied as user input or as a GraphQL literal.
   * @return The normalized string.
   */
  protected abstract String normalize(String inputValue) throws IllegalGraphqlArgumentException;

  private static void buildException(
      IllegalGraphqlArgumentException e,
      Consumer<String> messageConsumer,
      Consumer<List<SourceLocation>> sourceLocationsConsumer,
      Consumer<List<Object>> pathConsumer) {
    messageConsumer.accept(e.getMessage());

    // we have to specify a bogus source location and path; otherwise, graphql-spring-boot
    // will throw an NPE and return an empty response with a 400 response code
    //
    // CF
    // https://github.com/graphql-java/graphql-java/blob/v16.2/src/main/java/graphql/execution/ValuesResolver.java#L170
    //   for why source locations must be specified. If it is absent, the path property will become
    //   null.
    //
    // CF
    // https://github.com/graphql-java-kickstart/graphql-spring-boot/blob/v11.0.0/graphql-kickstart-spring-support/src/main/java/graphql/kickstart/spring/error/GraphQLErrorFromExceptionHandler.java#L79
    //   and
    // https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/GraphqlErrorBuilder.java#L77
    //   for why path must be specified.
    sourceLocationsConsumer.accept(Optional.ofNullable(e.getLocations()).orElseGet(List::of));
    pathConsumer.accept(Optional.ofNullable(e.getPath()).orElseGet(List::of));
  }
}
