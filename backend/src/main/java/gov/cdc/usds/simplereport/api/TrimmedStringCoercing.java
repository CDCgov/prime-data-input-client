package gov.cdc.usds.simplereport.api;

import java.util.Optional;

class TrimmedStringCoercing extends StringCoercing {
  TrimmedStringCoercing() {
    super("TrimmedString");
  }

  @Override
  protected String normalize(String inputValue) throws IllegalArgumentException {
    return Optional.ofNullable(inputValue).map(String::trim).orElse(null);
  }
}
