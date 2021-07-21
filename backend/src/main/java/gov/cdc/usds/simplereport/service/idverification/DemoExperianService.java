package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;
import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createSubmitAnswersRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsResponse;
import gov.cdc.usds.simplereport.api.model.errors.BadRequestException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@ConditionalOnProperty(value = "simple-report.experian.enabled", havingValue = "false")
@Service
public class DemoExperianService implements ExperianService {

  private static final ObjectMapper _objectMapper = new ObjectMapper();
  private static final List<Integer> EXPECTED_ANSWERS = Arrays.asList(1, 4, 2, 1);
  private static final String USER_EMAIL_NOT_FOUND = "notfound@example.com";

  private final Set<UUID> sessionIdSet;

  public DemoExperianService() {
    sessionIdSet = new HashSet<>();
  }

  public IdentityVerificationQuestionsResponse getQuestions(
      IdentityVerificationQuestionsRequest userData) {
    // next steps: this still needs proper implementation
    try {
      if (USER_EMAIL_NOT_FOUND.equals(userData.getEmail())) {
        throw new BadRequestException("No questions returned due to consumer not found");
      }

      createInitialRequestBody(
          "fakeSubcode",
          "fakeUsername",
          "fakePassword",
          "fakeTenantId",
          "fakeClientReferenceId",
          userData);

      JsonNode response =
          _objectMapper.readValue(
              "[{\"questionType\":28,\"questionText\":\"Please select the model year of the vehicle you purchased or leased prior to January 2011 .\",\"questionSelect\":{\"questionChoice\":[\"2002\",\"2003\",\"2004\",\"2005\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":24,\"questionText\":\"Which of the following professions do you currently or have If there is not a matched profession, please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"DENTIST / DENTAL HYGIENIST\",\"SOCIAL WORKER\",\"OPTICIAN / OPTOMETRIST\",\"ELECTRICIAN\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":41,\"questionText\":\"Please select the number of bedrooms in your home from the following choices. If the number of bedrooms in your home is not one of the choices please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"2\",\"3\",\"4\",\"5\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":47,\"questionText\":\"According to your credit profile, you may have opened a Home type loan in or around May 2015. Please select the lender to whom you currently made your payments.\",\"questionSelect\":{\"questionChoice\":[\"UC LENDING\",\"CTX MORTGAGE\",\"MID AMERICA MORTGAGE\",\"1ST NATIONWIDE MTG\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":49,\"questionText\":\"According to our records, you graduated from which of the following High Schools?\",\"questionSelect\":{\"questionChoice\":[\"BAXTER SPRINGS HIGH SCHOOL\",\"BELLS HIGH SCHOOL\",\"LOCKNEY HIGH SCHOOL\",\"AGUA DULCE HIGH SCHOOL\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}}]",
              ArrayNode.class);

      UUID sessionId = UUID.randomUUID();
      sessionIdSet.add(sessionId);

      return new IdentityVerificationQuestionsResponse(sessionId.toString(), response);
    } catch (RestClientException | NullPointerException | JsonProcessingException e) {
      throw new IllegalStateException("Questions could not be retrieved from Experian: ", e);
    }
  }

  public IdentityVerificationAnswersResponse submitAnswers(
      IdentityVerificationAnswersRequest answersRequest) {
    try {
      createSubmitAnswersRequestBody(
          "fakeSubcode",
          "fakeUsername",
          "fakePassword",
          "fakeTenantId",
          "fakeClientReferenceId",
          answersRequest);

      boolean passed = EXPECTED_ANSWERS.equals(answersRequest.getAnswers());

      UUID sessionUUID = UUID.fromString(answersRequest.getSessionId());
      if (sessionIdSet.contains(sessionUUID)) {
        sessionIdSet.remove(sessionUUID);
      } else {
        passed = false;
      }

      return new IdentityVerificationAnswersResponse(passed);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Answers could not be validated by Experian: ", e);
    }
  }

  // for test support

  public void addSessionId(UUID uuid) {
    sessionIdSet.add(uuid);
  }

  public void reset() {
    sessionIdSet.clear();
  }
}
