package gov.cdc.usds.simplereport.api;

import graphql.kickstart.servlet.apollo.ApolloScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScalarTypes {

  @Bean
  public GraphQLScalarType uploadScalar() {
    return ApolloScalars.Upload;
  }

  @Bean
  public GraphQLScalarType telephoneNumberScalar() {
    return GraphQLScalarType.newScalar()
        .name("TelephoneNumber")
        .description("A telephone number")
        .coercing(new TelephoneNumberCoercing())
        .build();
  }

  @Bean
  public GraphQLScalarType trimmedStringScalar() {
    return GraphQLScalarType.newScalar()
        .name("TrimmedString")
        .description("A string whose leading and trailing whitespace will be automatically removed")
        .coercing(new TrimmedStringCoercing())
        .build();
  }
}
