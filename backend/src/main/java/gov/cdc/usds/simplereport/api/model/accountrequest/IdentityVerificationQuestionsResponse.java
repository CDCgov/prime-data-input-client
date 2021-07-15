package gov.cdc.usds.simplereport.api.model.accountrequest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IdentityVerificationQuestionsResponse {

  private String sessionId;
  private JsonNode questionSet;
}
