package gov.cdc.usds.simplereport.api.model.accountrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IdentityVerificationAnswersResponse {

  public IdentityVerificationAnswersResponse(boolean passed) {
    this.passed = passed;
  }

  private boolean passed;
  // email address of account requester
  private String email;

  public void setEmail(String email) {
    this.email = email;
  }
}
