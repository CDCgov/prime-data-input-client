package gov.cdc.usds.simplereport.service.idverification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsResponse;
import gov.cdc.usds.simplereport.service.BaseServiceTest;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class DemoExperianServiceTest extends BaseServiceTest<DemoExperianService> {

  @Test
  void getQuestions_success() {
    IdentityVerificationQuestionsRequest request = createValidQuestionsRequest();
    IdentityVerificationQuestionsResponse response = _service.getQuestions(request);

    assertTrue(response.getQuestionSet().size() > 0);
    assertTrue(response.getQuestionSet().get(0).has("questionType"));
    assertTrue(response.getQuestionSet().get(0).has("questionText"));
    assertTrue(response.getQuestionSet().get(0).has("questionSelect"));
  }

  @Test
  void getQuestions_badArgument_fail() {
    IdentityVerificationQuestionsRequest request = new IdentityVerificationQuestionsRequest();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              _service.getQuestions(request);
            });
    assertThat(exception).hasMessageContaining("String is required and cannot be empty.");
  }

  @Test
  void submitAnswers_correctAnswers_success() {
    UUID uuid = UUID.randomUUID();
    _service.addSessionId(uuid);

    IdentityVerificationAnswersRequest request = createValidAnswersRequest(uuid);
    IdentityVerificationAnswersResponse response = _service.submitAnswers(request);

    assertTrue(response.isPassed());
  }

  @Test
  void submitAnswers_wrongAnswers_success() {
    UUID uuid = UUID.randomUUID();
    _service.addSessionId(uuid);

    IdentityVerificationAnswersRequest request = createValidAnswersRequest(uuid);
    request.getAnswers().set(0, 4);
    IdentityVerificationAnswersResponse response = _service.submitAnswers(request);

    assertFalse(response.isPassed());
  }

  private IdentityVerificationQuestionsRequest createValidQuestionsRequest() {
    IdentityVerificationQuestionsRequest request = new IdentityVerificationQuestionsRequest();
    request.setFirstName("First");
    request.setLastName("Last");
    request.setDateOfBirth("1945-06-07");
    request.setEmail("test@user.com");
    request.setPhoneNumber("800-555-1212");
    request.setStreetAddress1("1234 Main St.");
    request.setCity("Any City");
    request.setState("CA");
    request.setZip("90210");
    return request;
  }

  private IdentityVerificationAnswersRequest createValidAnswersRequest(UUID uuid) {
    IdentityVerificationAnswersRequest request = new IdentityVerificationAnswersRequest();
    request.setSessionId(uuid.toString());
    request.setAnswers(Arrays.asList(1, 4, 2, 1));
    return request;
  }
}
