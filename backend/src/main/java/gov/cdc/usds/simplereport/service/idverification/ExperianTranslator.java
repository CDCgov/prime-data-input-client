package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseState;

import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

/** Helper class to translate Experian requests and responses used for identity verification. */
public class ExperianTranslator {

  private ExperianTranslator() {
    throw new IllegalStateException("ExperianTranslator is a utility class");
  }

  public static final String INITIAL_REQUEST_CONTACTS =
      "{\"contacts\":[{\"id\":\"APPLICANT_CONTACT_ID_1\",\"person\":{\"typeOfPerson\":\"\",\"personIdentifier\":\"\",\"personDetails\":{\"dateOfBirth\":\"%s\",\"yearOfBirth\":\"\",\"age\":\"\",\"gender\":\"\",\"noOfDependents\":\"\",\"occupancyStatus\":\"\",\"mothersMaidenName\":\"\",\"spouseName\":\"\"},\"names\":[{\"id\":\"\",\"firstName\":\"%s\",\"middleNames\":\"%s\",\"surName\":\"%s\",\"nameSuffix\":\"\"}]},\"addresses\":[{\"id\":\"Main_Contact_Address_0\",\"addressType\":\"CURRENT\",\"poBoxNumber\":\"%s\",\"street\":\"%s\",\"street2\":\"%s\",\"postTown\":\"%s\",\"postal\":\"%s\",\"stateProvinceCode\":\"%s\"}],\"telephones\":[{\"id\":\"Main_Phone_0\",\"number\":\"%s\"}],\"emails\":[{\"id\":\"MAIN_EMAIL_0\",\"type\":\"\",\"email\":\"%s\"}]}]}";

  public static final String INITIAL_REQUEST_CONTROL =
      "{\"control\":[{\"option\":\"PIDXML_VERSION\",\"value\":\"06.00\"},{\"option\":\"SUBSCRIBER_PREAMBLE\",\"value\":\"TBD3\"},{\"option\":\"SUBSCRIBER_OPERATOR_INITIAL\",\"value\":\"CD\"},{\"option\":\"SUBSCRIBER_SUB_CODE\",\"value\":\"2923674\"},{\"option\":\"PID_USERNAME\",\"value\":\"%s\"},{\"option\":\"PID_PASSWORD\",\"value\":\"%s\"},{\"option\":\"VERBOSE\",\"value\":\"Y\"},{\"option\":\"PRODUCT_OPTION\",\"value\":\"24\"},{\"option\":\"DETAIL_REQUEST\",\"value\":\"D\"},{\"option\":\"VENDOR\",\"value\":\"123\"},{\"option\":\"VENDOR_VERSION\",\"value\":\"11\"},{\"option\":\"BROKER_NUMBER\",\"value\":\"\"},{\"option\":\"END_USER\",\"value\":\"\"},{\"option\":\"FREEZE_KEY_PIN\",\"value\":\"\"}]}";

  public static final String INITIAL_REQUEST_APPLICATION =
      "{\"application\":{\"productDetails\":{\"productType\":\"WRITTEN_INSTRUCTIONS\"},\"applicants\":[{\"contactId\":\"APPLICANT_CONTACT_ID_1\",\"applicantType\":\"CO_APPLICANT\"}]}}";

  /**
   * Using user-provided data, create the body of an initial Experian request.
   *
   * @param username SimpleReport's username to access Experian
   * @param password SimpleReport's password to access Experian
   * @param userData all user data associated with the request (name, phone number, etc)
   * @param tenantId the tenant id provided by Experian
   * @param clientReferenceId the client id provided by Experian
   * @return the JSON request body as a string
   */
  public static String createInitialRequestBody(
      String username,
      String password,
      IdentityVerificationRequest userData,
      String tenantId,
      String clientReferenceId) {
    String b64Password = new String(Base64.encodeBase64(password.getBytes()));
    // Create payload (includes user details)
    JSONObject contactBody = createContact(userData);
    JSONObject controlBody =
        new JSONObject(String.format(INITIAL_REQUEST_CONTROL, username, b64Password));
    JSONObject applicationBody = new JSONObject(INITIAL_REQUEST_APPLICATION);
    JSONObject payload = new JSONObject();
    payload.put("control", controlBody.get("control"));
    payload.put("contacts", contactBody.get("contacts"));
    payload.put("application", applicationBody.get("application"));

    // Create body-level headers
    JSONObject headers = new JSONObject();
    headers.put("tenantId", tenantId);
    headers.put("requestType", "PreciseIdOnly");
    headers.put("clientReferenceId", clientReferenceId);

    // Bundle response
    JSONObject response = new JSONObject();
    response.put("header", headers);
    response.put("payload", payload);
    return response.toString();
  }

  /**
   * Translates provided user data into a json "contacts" object. Validates required fields and
   * passes blank strings for optional fields.
   *
   * @param userData contains all the user-provided data required for this request.
   * @return a "contacts" object customized with the user's data.
   * @throws IllegalArgumentException if a required field is not present.
   */
  private static JSONObject createContact(IdentityVerificationRequest userData)
      throws IllegalArgumentException {
    String firstName = parseString(userData.getFirstName());
    String lastName = parseString(userData.getLastName());
    String middleName = parseOptionalString(userData.getMiddleName());
    String dob = parseDate(userData.getDateOfBirth());
    String email = parseEmail(parseString(userData.getEmail()));
    String phone = parseString(userData.getPhoneNumber());
    String street = parseString(userData.getStreetAddress1());
    String street2 = parseOptionalString(userData.getStreetAddress2());
    String postTown = parseString(userData.getCity());
    String stateCode = parseState(parseString(userData.getState()));
    String postal = parseString(userData.getZip());
    String poBoxNumber = parseOptionalString(userData.getPoBoxNumber());

    String requestBody =
        String.format(
            INITIAL_REQUEST_CONTACTS,
            dob,
            firstName,
            middleName,
            lastName,
            poBoxNumber,
            street,
            street2,
            postTown,
            postal,
            stateCode,
            phone,
            email);
    return new JSONObject(requestBody);
  }

  private static String parseString(String value) {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException("String is required and cannot be empty.");
    }
    return value.trim();
  }

  private static String parseOptionalString(String value) {
    return value == null ? "" : value.trim();
  }

  private static String parseDate(String d) {
    String date = parseString(d);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
    try {
      LocalDate.parse(date, formatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Date was not properly formatted. Dates should be formatted as yyyy-mm-dd.");
    }
    return date;
  }
}
