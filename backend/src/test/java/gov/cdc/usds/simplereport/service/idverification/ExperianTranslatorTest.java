package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import org.json.JSONObject;
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

  // positive tests

  @Test
  void requestBody_worksWithValidInput() throws Exception {
    IdentityVerificationRequest userData = createValidUserData();
    ObjectNode requestBody = createRequest(userData);

    //    JSONObject requestJson = new JSONObject(requestBody);
    assertThat(requestBody.has("payload")).isTrue();
    assertThat(requestBody.has("header")).isTrue();

    JsonNode payloadBody = requestBody.get("payload");
    assertThat(payloadBody.has("control")).isTrue();
    assertThat(payloadBody.has("contacts")).isTrue();

    JsonNode contactArray = payloadBody.get("contacts");
    assertThat(contactArray.size()).isEqualTo(1);

    JsonNode firstEntry = contactArray.get(0);
    JsonNode person = firstEntry.get("person");
    // names are as expected
    assertThat(person.at("/names/0/firstName").asText()).isEqualTo(FIRST_NAME);
    assertThat(person.at("/names/0/surName").asText()).isEqualTo(LAST_NAME);
    assertThat(person.at("/names/0/middleNames").asText()).isEqualTo(MIDDLE_NAME);

    // dob matches
    assertThat(person.at("/personDetails/dateOfBirth").asText()).isEqualTo(DOB);

    // contact information matches
    JsonNode address = firstEntry.at("/addresses/0");
    assertThat(address.get("street").asText()).isEqualTo(STREET_ADDRESS);
    assertThat(address.get("postTown").asText()).isEqualTo(CITY);
    assertThat(address.get("postal").asText()).isEqualTo(ZIP_CODE);
    assertThat(address.get("stateProvinceCode").asText()).isEqualTo(STATE);

    // email matches
    assertThat(firstEntry.at("/emails/0/email").asText()).isEqualTo(EMAIL);

    // phone number matches
    assertThat(firstEntry.at("/telephones/0/number").asText()).isEqualTo(PHONE);
  }

  void createRequest_successfulWithoutOptionalFields() throws Exception {
    IdentityVerificationRequest userData = createValidUserData();
    userData.setMiddleName(null);

    ObjectNode requestBody = createRequest(userData);

    JSONObject requestJson = new JSONObject(requestBody);
    assertThat(requestJson.has("payload")).isTrue();
    // other names still present
    assertThat(
            requestJson
                .getJSONObject("payload")
                .getJSONArray("contacts")
                .getJSONObject(0)
                .getJSONObject("person")
                .getJSONArray("names")
                .getJSONObject(0)
                .getString("firstName"))
        .isEqualTo(FIRST_NAME);
  }

  // negative tests

  @Test
  void createRequest_failsWithoutRequiredData() {
    IdentityVerificationRequest userData = createValidUserData();
    userData.setFirstName(null);
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              createRequest(userData);
            });
    assertThat(exception).hasMessageContaining("String is required and cannot be empty.");
  }

  @Test
  void createRequest_failsWithInvalidDate() {
    IdentityVerificationRequest userData = createValidUserData();
    userData.setDateOfBirth("180-12-03");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              createRequest(userData);
            });
    assertThat(exception).hasMessageContaining("Date was not properly formatted");
  }

  @Test
  void createRequest_failsWithInvalidDateFormat() {
    IdentityVerificationRequest userData = createValidUserData();
    userData.setDateOfBirth("1980/12/12");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              createRequest(userData);
            });
    assertThat(exception).hasMessageContaining("Date was not properly formatted");
  }

  // helpers

  private IdentityVerificationRequest createValidUserData() {
    IdentityVerificationRequest request = new IdentityVerificationRequest();
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

  private ObjectNode createRequest(IdentityVerificationRequest userData)
      throws JsonProcessingException {
    return createInitialRequestBody(
        "subscriberSubcode", "username", "password", "tenantId", "clientReferenceId", userData);
  }
}
