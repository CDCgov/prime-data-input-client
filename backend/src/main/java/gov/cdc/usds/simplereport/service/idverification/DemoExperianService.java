package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(value = "simple-report.experian.enabled", havingValue = "false")
@Service
public class DemoExperianService implements ExperianService {

  private static final ObjectMapper _objectMapper = new ObjectMapper();

  public String fetchToken() {
    return "accessToken";
  }

  public JsonNode getQuestions(IdentityVerificationRequest userData) {
    // next steps: this still needs proper implementation
    try {
      ObjectNode initialRequestBody =
          createInitialRequestBody(
              "fakeSubcode",
              "fakeUsername",
              "fakePassword",
              "fakeTenantId",
              "fakeClientReferenceId",
              userData);
      return _objectMapper.readValue(
          "{\"questionSet\":[{\"questionType\":28,\"questionText\":\"Please select the model year of the vehicle you purchased or leased prior to January 2011 .\",\"questionSelect\":{\"questionChoice\":[\"2002\",\"2003\",\"2004\",\"2005\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":24,\"questionText\":\"Which of the following professions do you currently or have If there is not a matched profession, please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"DENTIST / DENTAL HYGIENIST\",\"SOCIAL WORKER\",\"OPTICIAN / OPTOMETRIST\",\"ELECTRICIAN\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":41,\"questionText\":\"Please select the number of bedrooms in your home from the following choices. If the number of bedrooms in your home is not one of the choices please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"2\",\"3\",\"4\",\"5\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":47,\"questionText\":\"According to your credit profile, you may have opened a Home type loan in or around May 2015. Please select the lender to whom you currently made your payments.\",\"questionSelect\":{\"questionChoice\":[\"UC LENDING\",\"CTX MORTGAGE\",\"MID AMERICA MORTGAGE\",\"1ST NATIONWIDE MTG\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":49,\"questionText\":\"According to our records, you graduated from which of the following High Schools?\",\"questionSelect\":{\"questionChoice\":[\"BAXTER SPRINGS HIGH SCHOOL\",\"BELLS HIGH SCHOOL\",\"LOCKNEY HIGH SCHOOL\",\"AGUA DULCE HIGH SCHOOL\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}}]}",
          JsonNode.class);
    } catch (RestClientException | NullPointerException | JsonProcessingException e) {
      throw new IllegalStateException("Questions could not be retrieved from Experian: ", e);
    }
  }

  public IdentityVerificationAnswersResponse submitAnswers(
      IdentityVerificationAnswersRequest answerRequest) {
    return new IdentityVerificationAnswersResponse(false);
  }

  public void reset() {
    // clear any state variables
  }
}
