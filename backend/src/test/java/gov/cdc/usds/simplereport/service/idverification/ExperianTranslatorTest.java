package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;
import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createSubmitAnswersRequestBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExperianTranslatorTest {

  private static final String FIRST_NAME = "Jane";
  private static final String LAST_NAME = "Doe";
  private static final String MIDDLE_NAME = "M";
  private static final String DOB = "1980-03-21";
  private static final String EMAIL = "jane@example.com";
  private static final String PHONE = "410-867-4309";
  private static final String STREET_ADDRESS = "1600 Pennsylvania Ave SE";
  private static final String CITY = "Washington";
  private static final String STATE = "DC";
  private static final String ZIP_CODE = "20500";

  private static final String SESSION_UUID = "1a6fe862-1661-47a9-a94c-d0fb3f2b9ecf";
  private static final List<Integer> CORRECT_RESPONSES = Arrays.asList(1, 4, 2, 1);

  // positive tests

  @Test
  void createInitialRequestBody_worksWithValidInput() throws Exception {
    IdentityVerificationQuestionsRequest userData = createValidUserData();
    ObjectNode requestBody = initialRequestHelper(userData);

    assertTrue(requestBody.has("payload"));
    assertTrue(requestBody.has("header"));

    JsonNode payloadBody = requestBody.get("payload");
    assertTrue(payloadBody.has("control"));
    assertTrue(payloadBody.has("contacts"));

    JsonNode contactArray = payloadBody.get("contacts");
    assertEquals(1, contactArray.size());

    JsonNode firstEntry = contactArray.get(0);
    JsonNode person = firstEntry.get("person");
    // names are as expected
    assertEquals(FIRST_NAME, person.at("/names/0/firstName").asText());
    assertEquals(LAST_NAME, person.at("/names/0/surName").asText());
    assertEquals(MIDDLE_NAME, person.at("/names/0/middleNames").asText());

    // dob matches
    assertEquals(DOB, person.at("/personDetails/dateOfBirth").asText());

    // contact information matches
    JsonNode address = firstEntry.at("/addresses/0");
    assertEquals(STREET_ADDRESS, address.get("street").asText());
    assertEquals(CITY, address.get("postTown").asText());
    assertEquals(ZIP_CODE, address.get("postal").asText());
    assertEquals(STATE, address.get("stateProvinceCode").asText());

    // email matches
    assertEquals(EMAIL, firstEntry.at("/emails/0/email").asText());

    // phone number matches
    assertEquals(PHONE, firstEntry.at("/telephones/0/number").asText());
  }

  @Test
  void createInitialRequestBody_successfulWithoutOptionalFields() throws Exception {
    IdentityVerificationQuestionsRequest userData = createValidUserData();
    userData.setMiddleName(null);

    ObjectNode requestBody = initialRequestHelper(userData);

    assertTrue(requestBody.has("payload"));
    // other names still present
    assertEquals(
        FIRST_NAME, requestBody.at("/payload/contacts/0/person/names/0/firstName").asText());
  }

  // negative tests

  @Test
  void createInitialRequestBody_failsWithoutRequiredData() {
    IdentityVerificationQuestionsRequest userData = createValidUserData();
    userData.setFirstName(null);
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              initialRequestHelper(userData);
            });
    assertThat(exception).hasMessageContaining("String is required and cannot be empty.");
  }

  @Test
  void createInitialRequestBody_failsWithInvalidDate() {
    IdentityVerificationQuestionsRequest userData = createValidUserData();
    userData.setDateOfBirth("180-12-03");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              initialRequestHelper(userData);
            });
    assertThat(exception).hasMessageContaining("Date was not properly formatted");
  }

  @Test
  void createInitialRequestBody_failsWithInvalidDateFormat() {
    IdentityVerificationQuestionsRequest userData = createValidUserData();
    userData.setDateOfBirth("1980/12/12");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              initialRequestHelper(userData);
            });
    assertThat(exception).hasMessageContaining("Date was not properly formatted");
  }

  @Test
  void createSubmitAnswersRequestBody_worksWithValidInput() throws JsonProcessingException {
    IdentityVerificationAnswersRequest answersRequest = createValidAnswersRequest();
    ObjectNode requestBody = finalRequestHelper(answersRequest);

    assertTrue(requestBody.has("payload"));
    assertTrue(requestBody.has("header"));

    JsonNode payloadBody = requestBody.get("payload");
    assertTrue(payloadBody.has("control"));
    assertTrue(payloadBody.has("kba"));

    JsonNode kbaNode = payloadBody.get("kba");
    assertEquals(SESSION_UUID, kbaNode.get("sessionId").asText());
    assertEquals("4", kbaNode.get("outWalletQuestionsRequest").asText());

    JsonNode answersNode = kbaNode.get("answers");
    assertEquals(4, answersNode.size());
    for (int i = 0; i < 4; i++) {
      assertEquals(
          String.valueOf(CORRECT_RESPONSES.get(i)),
          answersNode.get("outWalletAnswer" + (i + 1)).asText());
    }
  }

  @Test
  void createSubmitAnswersRequestBody_failsWithMissingUuid() throws JsonProcessingException {
    IdentityVerificationAnswersRequest answersRequest = createValidAnswersRequest();
    answersRequest.setSessionId("");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              finalRequestHelper(answersRequest);
            });
    assertThat(exception).hasMessageContaining("String is required and cannot be empty.");
  }

  // helpers

  private IdentityVerificationQuestionsRequest createValidUserData() {
    IdentityVerificationQuestionsRequest request = new IdentityVerificationQuestionsRequest();
    request.setFirstName(FIRST_NAME);
    request.setLastName(LAST_NAME);
    request.setMiddleName(MIDDLE_NAME);
    request.setDateOfBirth(DOB);
    request.setEmail(EMAIL);
    request.setPhoneNumber(PHONE);
    request.setStreetAddress1(STREET_ADDRESS);
    request.setCity(CITY);
    request.setState(STATE);
    request.setZip(ZIP_CODE);
    return request;
  }

  private ObjectNode initialRequestHelper(IdentityVerificationQuestionsRequest userData)
      throws JsonProcessingException {
    return createInitialRequestBody(
        "subscriberSubcode", "username", "password", "tenantId", "clientReferenceId", userData);
  }

  private IdentityVerificationAnswersRequest createValidAnswersRequest() {
    IdentityVerificationAnswersRequest request = new IdentityVerificationAnswersRequest();
    request.setSessionId(SESSION_UUID);
    request.setAnswers(CORRECT_RESPONSES);
    return request;
  }

  private ObjectNode finalRequestHelper(IdentityVerificationAnswersRequest answersRequest)
      throws JsonProcessingException {
    return createSubmitAnswersRequestBody(
        "subscriberSubcode",
        "username",
        "password",
        "tenantId",
        "clientReferenceId",
        answersRequest);
  }
}
