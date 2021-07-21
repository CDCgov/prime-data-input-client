package gov.cdc.usds.simplereport.api;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gov.cdc.usds.simplereport.api.accountrequest.IdentityVerificationController;
import gov.cdc.usds.simplereport.config.TemplateConfiguration;
import gov.cdc.usds.simplereport.config.WebConfiguration;
import gov.cdc.usds.simplereport.config.authorization.DemoAuthenticationConfiguration;
import gov.cdc.usds.simplereport.idp.repository.DemoOktaRepository;
import gov.cdc.usds.simplereport.logging.AuditLoggingAdvice;
import gov.cdc.usds.simplereport.service.ApiUserService;
import gov.cdc.usds.simplereport.service.OrganizationService;
import gov.cdc.usds.simplereport.service.idverification.DemoExperianService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import({
  DemoAuthenticationConfiguration.class,
  DemoExperianService.class,
  DemoOktaRepository.class
})
@WebMvcTest(
    controllers = IdentityVerificationController.class,
    includeFilters =
        @Filter(
            classes = {TemplateConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE),
    excludeFilters =
        @Filter(
            classes = {AuditLoggingAdvice.class, WebConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE))
class IdentityVerificationControllerTest {
  @Autowired private MockMvc _mockMvc;

  @Autowired private DemoExperianService _experianService;
  @MockBean private OrganizationService _orgService;

  // Dependencies of TenantDataAccessFilter
  @MockBean private ApiUserService _mockApiUserService;
  @MockBean private CurrentTenantDataAccessContextHolder _mockContextHolder;

  private static final String VALID_GET_QUESTIONS_REQUEST =
      "{\"firstName\":\"Jane\", \"lastName\":\"Doe\", \"dateOfBirth\":\"1980-08-12\", \"email\":\"jane@example.com\", \"phoneNumber\":\"410-867-5309\", \"streetAddress1\":\"1600 Pennsylvania Ave\", \"city\":\"Washington\", \"state\":\"DC\", \"zip\":\"20500\"}";

  private static final String FAKE_ORG_EXTERNAL_ID = "FAKE_ORG_EXTERNAL_ID";
  private static final UUID VALID_SESSION_UUID =
      UUID.fromString("099244e0-bebc-4f59-83fd-453dc7f0b858");
  private static final String SUBMIT_ANSWERS_CORRECT_REQUEST =
      "{\"orgExternalId\": \""
          + FAKE_ORG_EXTERNAL_ID
          + "\", \"sessionId\": \""
          + VALID_SESSION_UUID
          + "\", \"answers\": [1, 4, 2, 1]}";
  private static final String SUBMIT_ANSWERS_INCORRECT_REQUEST =
      "{\"orgExternalId\": \""
          + FAKE_ORG_EXTERNAL_ID
          + "\", \"sessionId\": \""
          + VALID_SESSION_UUID
          + "\", \"answers\": [4, 3, 2, 1]}";

  @BeforeEach
  public void setup() throws Exception {
    _experianService.reset();
  }

  @Test
  void getQuestions_isOk() throws Exception {
    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ID_VERIFICATION_GET_QUESTIONS)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_GET_QUESTIONS_REQUEST);

    this._mockMvc
        .perform(builder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId", any(String.class)))
        .andExpect(jsonPath("$.questionSet", any(List.class)));
  }

  @Test
  void submitAnswers_correctAnswer_success() throws Exception {
    _experianService.addSessionId(VALID_SESSION_UUID);

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ID_VERIFICATION_SUBMIT_ANSWERS)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(SUBMIT_ANSWERS_CORRECT_REQUEST);

    this._mockMvc
        .perform(builder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.passed", is(true)));
  }

  @Test
  void submitAnswers_incorrectAnswers_success() throws Exception {
    _experianService.addSessionId(VALID_SESSION_UUID);

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ID_VERIFICATION_SUBMIT_ANSWERS)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(SUBMIT_ANSWERS_INCORRECT_REQUEST);

    this._mockMvc
        .perform(builder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.passed", is(false)));
  }
}
