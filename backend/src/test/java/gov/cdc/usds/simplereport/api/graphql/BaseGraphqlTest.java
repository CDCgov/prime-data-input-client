package gov.cdc.usds.simplereport.api.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import gov.cdc.usds.simplereport.api.BaseFullStackTest;
import gov.cdc.usds.simplereport.api.model.Role;
import gov.cdc.usds.simplereport.config.authorization.DemoAuthenticationConfiguration;
import gov.cdc.usds.simplereport.config.simplereport.DemoUserConfiguration;
import gov.cdc.usds.simplereport.config.simplereport.DemoUserConfiguration.DemoUser;
import gov.cdc.usds.simplereport.idp.repository.DemoOktaRepository;
import gov.cdc.usds.simplereport.service.AddressValidationService;
import gov.cdc.usds.simplereport.service.OrganizationInitializingService;
import gov.cdc.usds.simplereport.test_util.TestUserIdentities;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Base class for GraphQL API full-stack tests. */
public abstract class BaseGraphqlTest extends BaseFullStackTest {

  private static final Logger LOG = LoggerFactory.getLogger(BaseGraphqlTest.class);

  protected static final String ACCESS_ERROR =
      "Current user does not have permission for this action";

  @Autowired private OrganizationInitializingService _initService;
  @Autowired private DemoOktaRepository _oktaRepo;
  @Autowired private GraphQLTestTemplate _template;
  @Autowired private DemoUserConfiguration _users;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private AddressValidationService _addressValidation;

  private String _userName = null;
  private MultiValueMap<String, String> _customHeaders;
  private ResponseEntity<String> _lastResponse;

  protected void useOrgUser() {
    _userName = TestUserIdentities.STANDARD_USER;
  }

  protected void useOutsideOrgUser() {
    _userName = TestUserIdentities.OTHER_ORG_USER;
  }

  protected void useOrgAdmin() {
    _userName = TestUserIdentities.ORG_ADMIN_USER;
  }

  protected void useOutsideOrgAdmin() {
    _userName = TestUserIdentities.OTHER_ORG_ADMIN;
  }

  protected void useOrgEntryOnly() {
    _userName = TestUserIdentities.ENTRY_ONLY_USER;
  }

  protected void useOrgUserAllFacilityAccess() {
    _userName = TestUserIdentities.ALL_FACILITIES_USER;
  }

  protected void useSuperUser() {
    _userName = TestUserIdentities.SITE_ADMIN_USER;
  }

  protected void useSuperUserWithOrg() {
    _userName = TestUserIdentities.SITE_ADMIN_USER_WITH_ORG;
  }

  protected void useBrokenUser() {
    _userName = TestUserIdentities.BROKEN_USER;
  }

  /**
   * Add a custom header to a <b>single request</b> to be performed using {@link #runQuery}.
   *
   * @param name the HTTP header name (potentially subject to weird transformations--check your
   *     work!)
   * @param value the header value to be sent.
   */
  protected void addHeader(String name, String value) {
    _customHeaders.add(name, value);
  }

  protected ResponseEntity<String> getLastResponse() {
    return _lastResponse;
  }

  @BeforeEach
  public void setup() {
    truncateDb();
    _oktaRepo.reset();
    when(_addressValidation.getValidatedAddress(any(), any()))
        .thenReturn(_dataFactory.getAddress());
    TestUserIdentities.withStandardUser(_initService::initAll);
    useOrgUser();
    _customHeaders = new LinkedMultiValueMap<String, String>();
    _lastResponse = null;
    assertNull(
        // Dear future reader: this is not negotiable. If you set a default user, then patients will
        // show up as being the default user instead of themselves. This would be bad.
        _users.getDefaultUser(), "default user should never be set in this application context");
    LOG.trace(
        "Usernames configured: {}",
        _users.getAllUsers().stream().map(DemoUser::getUsername).collect(Collectors.toList()));
  }

  @AfterEach
  public void cleanup() {
    truncateDb();
    _userName = null;
    _oktaRepo.reset();
  }

  private String getBearerAuth() {
    return DemoAuthenticationConfiguration.DEMO_AUTHORIZATION_FLAG + _userName;
  }

  /**
   * CLEAR ALL HEADERS and then set the Authorization header the requested value, as well as any
   * other custom headers supplied for this request.
   */
  private void setQueryHeaders() {
    LOG.info("Setting up graphql template authorization for {}", _userName);
    LOG.info("Setting custom headers: {}", _customHeaders.keySet());
    _template
        .withClearHeaders()
        .withBearerAuth(getBearerAuth())
        .withAdditionalHeaders(_customHeaders);
    _customHeaders.clear();
  }

  protected ObjectNode runMultipart(LinkedMultiValueMap<String, Object> parts) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(getBearerAuth());
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> request =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(parts, headers);
    try {
      _lastResponse = restTemplate.exchange("/graphql", HttpMethod.POST, request, String.class);
      assertEquals(HttpStatus.OK, _lastResponse.getStatusCode(), "Servlet response should be OK");
      GraphQLResponse response = new GraphQLResponse(_lastResponse, objectMapper);
      JsonNode responseBody = response.readTree();
      assertGraphQLOutcome(responseBody, null);
      return (ObjectNode) responseBody.get("data");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** See {@link #runQuery(String, String, ObjectNode, String)}. */
  protected ObjectNode runQuery(String queryFileName) {
    return runQuery(queryFileName, null, null, null);
  }

  /**
   * Run the query in the given resource file, check if the response has the expected error (either
   * none or a single specific error message), and return the {@code data} section of the response
   * if the error was as expected.
   */
  protected ObjectNode runQuery(String queryFileName, String expectedError) {
    return runQuery(queryFileName, null, null, expectedError);
  }

  /** See {@link #runQuery(String,String,ObjectNode,String)}. */
  protected ObjectNode runQuery(String queryFileName, ObjectNode variables, String expectedError) {
    return runQuery(queryFileName, null, variables, expectedError);
  }

  /**
   * Run the query in the given resource file, check if the response has the expected error (either
   * none or a single specific error message), and return the {@code data} section of the response
   * if the error was as expected. <b>NOTE</b>: Any headers that have been set on the {@link
   * GraphQLTestTemplate} will be cleared at the beginning of this method: if you need to set them,
   * modify the {{@link #setQueryHeaders(String)} method, or add another method that is called after
   * it!
   *
   * @param queryFileName the resource file name of the query (to be found in
   *     src/test/resources/queries, unless a "/" is found in the filename)
   * @param operationName the operation name from the query file, in the event that the query file
   *     is a multi-operation document (apparently). This turns out not to be needed for any of our
   *     cases, but we can leave it supported for the day when it is.
   * @return the "data" key from the server response.
   * @throws AssertionFailedError if the response has errors
   * @throws RuntimeException for unexpected errors
   */
  protected ObjectNode runQuery(
      String queryFileName, String operationName, ObjectNode variables, String expectedError) {
    if (queryFileName != null && !queryFileName.contains("/")) {
      queryFileName = "queries/" + queryFileName;
    }
    try {
      setQueryHeaders();
      GraphQLResponse response = _template.perform(queryFileName, operationName, variables);
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Servlet response should be OK");
      _lastResponse = response.getRawResponse();
      JsonNode responseBody = response.readTree();
      assertGraphQLOutcome(responseBody, expectedError);
      return Optional.ofNullable(responseBody.get("data"))
          .filter(ObjectNode.class::isInstance)
          .map(ObjectNode.class::cast)
          .orElse(null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected ObjectNode runQuery(String queryFileName, ObjectNode variables) {
    return runQuery(queryFileName, null, variables, null);
  }

  /**
   * Check if the given response body has an {@code errors} section. If so, if we have an expected
   * error, check that the errors section contains it; if we do not, then fail the test.
   *
   * @param expectedError if null, expect success; if not null, expect this error message
   */
  protected static void assertGraphQLOutcome(JsonNode responseBody, String expectedError) {
    JsonNode errorNode = responseBody.path("errors");
    if (null == expectedError) {
      if (!errorNode.isMissingNode()) {
        fail(errorNode.toString());
      }
    } else {
      assertThat(errorNode.get(0).get("message").asText()).contains(expectedError);
    }
  }

  protected ObjectNode executeAddPersonMutation(
      String firstName,
      String lastName,
      String birthDate,
      String phone,
      String lookupId,
      Optional<UUID> facilityId,
      Optional<String> expectedError)
      throws IOException {
    ObjectNode variables =
        JsonNodeFactory.instance
            .objectNode()
            .put("firstName", firstName)
            .put("lastName", lastName)
            .put("birthDate", birthDate)
            .put("telephone", phone)
            .put("lookupId", lookupId)
            .put("facilityId", facilityId.map(UUID::toString).orElse(null));
    return runQuery("add-person", variables, expectedError.orElse(null));
  }

  protected void updateSelfPrivileges(
      Role role, boolean accessAllFacilities, Set<UUID> facilities) {
    String originalUsername = _userName;

    ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
    String id = who.get("id").asText();

    // cannot update own privileges, so need to switch to another profile
    if (originalUsername.equals(TestUserIdentities.ORG_ADMIN_USER)) {
      useSuperUser();
    } else {
      useOrgAdmin();
    }

    ObjectNode updatePrivilegesVariables =
        getUpdateUserPrivilegesVariables(id, role, accessAllFacilities, facilities);
    runQuery("update-user-privileges", updatePrivilegesVariables);

    _userName = originalUsername;
  }

  protected ObjectNode getUpdateUserPrivilegesVariables(
      String id, Role role, boolean accessAllFacilities, Set<UUID> facilities) {
    ObjectNode variables =
        JsonNodeFactory.instance
            .objectNode()
            .put("id", id)
            .put("role", role.name())
            .put("accessAllFacilities", accessAllFacilities);
    variables
        .putArray("facilities")
        .addAll(
            facilities.stream()
                .map(f -> JsonNodeFactory.instance.textNode(f.toString()))
                .collect(Collectors.toSet()));
    return variables;
  }

  // map from each facility's name to its UUID; includes all facilities in organization
  protected Map<String, UUID> extractAllFacilitiesInOrg() {
    String originalUsername = _userName;
    useOrgAdmin();
    Iterator<JsonNode> facilitiesIter =
        runQuery("org-settings-query").get("organization").get("testingFacility").elements();
    Map<String, UUID> facilities = new HashMap<>();
    while (facilitiesIter.hasNext()) {
      JsonNode facility = facilitiesIter.next();
      facilities.put(facility.get("name").asText(), UUID.fromString(facility.get("id").asText()));
    }
    _userName = originalUsername;
    return facilities;
  }
}
