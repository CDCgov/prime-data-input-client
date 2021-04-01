package gov.cdc.usds.simplereport.api;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;

class TelephoneNumberCoercing extends StringCoercing {
  TelephoneNumberCoercing() {
    super("TelephoneNumber");
  }

  @Override
  protected String normalize(String inputValue) throws IllegalGraphqlArgumentException {
    return Translators.parsePhoneNumber(inputValue);
  }
}
