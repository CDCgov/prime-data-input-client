package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseState;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;

/** Helper class to translate Experian requests and responses used for identity verification. */
public class ExperianTranslator {

  private ExperianTranslator() {
    throw new IllegalStateException("ExperianTranslator is a utility class");
  }

  private static final ObjectMapper _objectMapper = new ObjectMapper();

  public static final String INITIAL_REQUEST_CONTACTS =
      "[{\"id\":\"APPLICANT_CONTACT_ID_1\",\"person\":{\"typeOfPerson\":\"\",\"personIdentifier\":\"\",\"personDetails\":{\"dateOfBirth\":\"%s\",\"yearOfBirth\":\"\",\"age\":\"\",\"gender\":\"\",\"noOfDependents\":\"\",\"occupancyStatus\":\"\",\"mothersMaidenName\":\"\",\"spouseName\":\"\"},\"names\":[{\"id\":\"\",\"firstName\":\"%s\",\"middleNames\":\"%s\",\"surName\":\"%s\",\"nameSuffix\":\"\"}]},\"addresses\":[{\"id\":\"Main_Contact_Address_0\",\"addressType\":\"CURRENT\",\"poBoxNumber\":\"%s\",\"street\":\"%s\",\"street2\":\"%s\",\"postTown\":\"%s\",\"postal\":\"%s\",\"stateProvinceCode\":\"%s\"}],\"telephones\":[{\"id\":\"Main_Phone_0\",\"number\":\"%s\"}],\"emails\":[{\"id\":\"MAIN_EMAIL_0\",\"type\":\"\",\"email\":\"%s\"}]}]";

  public static final String INITIAL_REQUEST_CONTROL =
      "[{\"option\":\"PIDXML_VERSION\",\"value\":\"06.00\"},{\"option\":\"SUBSCRIBER_PREAMBLE\",\"value\":\"TBD3\"},{\"option\":\"SUBSCRIBER_OPERATOR_INITIAL\",\"value\":\"CD\"},{\"option\":\"SUBSCRIBER_SUB_CODE\",\"value\":\"%s\"},{\"option\":\"PID_USERNAME\",\"value\":\"%s\"},{\"option\":\"PID_PASSWORD\",\"value\":\"%s\"},{\"option\":\"VERBOSE\",\"value\":\"Y\"},{\"option\":\"PRODUCT_OPTION\",\"value\":\"24\"},{\"option\":\"DETAIL_REQUEST\",\"value\":\"D\"},{\"option\":\"VENDOR\",\"value\":\"123\"},{\"option\":\"VENDOR_VERSION\",\"value\":\"11\"},{\"option\":\"BROKER_NUMBER\",\"value\":\"\"},{\"option\":\"END_USER\",\"value\":\"\"},{\"option\":\"FREEZE_KEY_PIN\",\"value\":\"\"}]";

  public static final String INITIAL_REQUEST_APPLICATION =
      "{\"productDetails\":{\"productType\":\"WRITTEN_INSTRUCTIONS\"},\"applicants\":[{\"contactId\":\"APPLICANT_CONTACT_ID_1\",\"applicantType\":\"CO_APPLICANT\"}]}";

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
  public static ObjectNode createInitialRequestBody(
      String subscriberSubcode,
      String username,
      String password,
      String tenantId,
      String clientReferenceId,
      IdentityVerificationRequest userData)
      throws JsonProcessingException {
    String b64Password = new String(Base64.encodeBase64(password.getBytes()));
    final JsonNodeFactory factory = JsonNodeFactory.instance;

    // Create payload (includes user details)
    JsonNode contactBody = createContact(userData);
    JsonNode controlBody =
        _objectMapper.readValue(
            String.format(INITIAL_REQUEST_CONTROL, subscriberSubcode, username, b64Password),
            ArrayNode.class);
    ObjectNode applicationBody =
        _objectMapper.readValue(INITIAL_REQUEST_APPLICATION, ObjectNode.class);

    ObjectNode payload = factory.objectNode();
    payload.set("control", controlBody);
    payload.set("contacts", contactBody);
    payload.set("application", applicationBody);

    Date nowDate = Date.from(Instant.now());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    String nowDateString = formatter.format(nowDate);

    // Create body-level headers
    ObjectNode headers = factory.objectNode();
    headers.put("tenantId", tenantId);
    headers.put("requestType", "PreciseIdOnly");
    headers.put("clientReferenceId", clientReferenceId);
    headers.set("expRequestId", factory.nullNode());
    headers.put("messageTime", nowDateString);
    headers.put("txnId", "");
    headers.put("time", "");
    headers.set("options", factory.objectNode());

    // Bundle response
    ObjectNode response = factory.objectNode();
    response.set("header", headers);
    response.set("payload", payload);
    return response;
  }

  /**
   * Translates provided user data into a json "contacts" object. Validates required fields and
   * passes blank strings for optional fields.
   *
   * @param userData contains all the user-provided data required for this request.
   * @return a "contacts" object customized with the user's data.
   * @throws IllegalArgumentException if a required field is not present.
   */
  private static JsonNode createContact(IdentityVerificationRequest userData)
      throws IllegalArgumentException, JsonProcessingException {
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

    return _objectMapper.readValue(
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
            email),
        ArrayNode.class);
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
