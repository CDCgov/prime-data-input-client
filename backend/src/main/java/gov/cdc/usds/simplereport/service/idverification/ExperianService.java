package gov.cdc.usds.simplereport.service.idverification;

import com.fasterxml.jackson.databind.JsonNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;

public interface ExperianService {

  /** Retrieves questions from Experian, given user data. */
  public JsonNode getQuestions(IdentityVerificationRequest userData);

  public IdentityVerificationAnswersResponse submitAnswers(
      IdentityVerificationAnswersRequest answerRequest);
}
