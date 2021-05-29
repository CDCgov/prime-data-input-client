package gov.cdc.usds.simplereport.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gov.cdc.usds.simplereport.api.apiuser.UserAccountCreationController;
import gov.cdc.usds.simplereport.config.TemplateConfiguration;
import gov.cdc.usds.simplereport.config.WebConfiguration;
import gov.cdc.usds.simplereport.idp.authentication.DemoOktaAuthentication;
import gov.cdc.usds.simplereport.logging.AuditLoggingAdvice;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(DemoOktaAuthentication.class)
@WebMvcTest(
    controllers = UserAccountCreationController.class,
    includeFilters =
        @Filter(
            classes = {TemplateConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE),
    excludeFilters =
        @Filter(
            classes = {AuditLoggingAdvice.class, WebConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE))
class UserAccountCreationControllerTest {

  @Autowired private MockMvc _mockMvc;

  @Autowired private DemoOktaAuthentication _oktaAuth;

  private static final String VALID_PASSWORD_REQUEST =
      "{\"activationToken\":\"validActivationToken\", \"password\":\"superStrongPassword!\"}";

  private static final String VALID_RECOVERY_QUESTION_REQUEST =
      "{\"question\":\"Who was your third grade teacher?\", \"answer\" : \"Jane Doe\"}";

  private static final String VALID_ENROLL_PHONE_MFA_REQUEST = "{\"userInput\":\"555-867-5309\"}";

  private static final String VALID_ENROLL_EMAIL_MFA_REQUEST = "{\"userInput\":\"me@example.com\"}";

  @BeforeEach
  public void setup() throws Exception {
    _oktaAuth.reset();
  }

  @Test
  void setPasswordIsOk() throws Exception {
    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST);

    this._mockMvc.perform(builder).andExpect(status().isOk());
  }

  @Test
  void setPassword_failsWithoutActivationToken() throws Exception {
    String passwordRequestNoActivation = "{\"password\":\"superStrongPassword!\"}";

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(passwordRequestNoActivation);

    this._mockMvc.perform(builder).andExpect(status().isForbidden());
  }

  @Test
  void setPassword_worksAsExpectedWithMultipleSessions() throws Exception {
    MockHttpServletRequestBuilder firstBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST);

    String secondValidPasswordRequest =
        "{\"activationToken\":\"anotherValidAuthToken\", \"password\":\"secondSuperStrongPassword!?\"}";

    MockHttpServletRequestBuilder secondBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(secondValidPasswordRequest);

    HttpSession firstSession =
        this._mockMvc
            .perform(firstBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    HttpSession secondSession =
        this._mockMvc
            .perform(secondBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    assertThat(firstSession.getId()).isNotEqualTo(secondSession.getId());
    assertThat(firstSession.getAttribute("userId")).isEqualTo("userId " + "validActivationToken");
  }

  @Test
  void setPasswordThenRecoveryQuestions() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder setPasswordBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder setRecoveryQuestionBuilder =
        post(ResourceLinks.USER_SET_RECOVERY_QUESTION)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_RECOVERY_QUESTION_REQUEST)
            .session(session);

    HttpSession setPasswordResponse =
        this._mockMvc
            .perform(setPasswordBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    HttpSession setRecoveryQuestionResponse =
        this._mockMvc
            .perform(setRecoveryQuestionBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    // assert that the userId is propagated to the recovery question session
    assertThat(setRecoveryQuestionResponse.getAttribute("userId"))
        .isEqualTo("userId " + "validActivationToken");
    assertEquals(setPasswordResponse, setRecoveryQuestionResponse);
  }

  @Test
  void enrollSmsMfaIsOk() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder setPasswordBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder enrollSmsMfaBuilder =
        post(ResourceLinks.USER_ENROLL_SMS_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_PHONE_MFA_REQUEST)
            .session(session);

    HttpSession setPasswordResponse =
        this._mockMvc
            .perform(setPasswordBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    HttpSession enrollSmsMfaResponse =
        this._mockMvc
            .perform(enrollSmsMfaBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    assertThat(setPasswordResponse.getAttribute("userId"))
        .isEqualTo(enrollSmsMfaResponse.getAttribute("userId"));
    assertThat(enrollSmsMfaResponse.getAttribute("factorId")).isNotNull();
  }

  @Test
  void cannotEnrollSmsMfa_withoutActivatedUser() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder enrollSmsMfaBuilder =
        post(ResourceLinks.USER_ENROLL_SMS_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_PHONE_MFA_REQUEST)
            .session(session);

    this._mockMvc.perform(enrollSmsMfaBuilder).andExpect(status().is4xxClientError());
  }

  @Test
  void enrollVoiceCallMfaIsOk() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder activateUserBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder enrollVoiceCallMfaBuilder =
        post(ResourceLinks.USER_ENROLL_VOICE_CALL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_PHONE_MFA_REQUEST)
            .session(session);

    HttpSession setPasswordResponse =
        this._mockMvc
            .perform(activateUserBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    HttpSession enrollVoiceCallMfaResponse =
        this._mockMvc
            .perform(enrollVoiceCallMfaBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    assertThat(setPasswordResponse.getAttribute("userId"))
        .isEqualTo(enrollVoiceCallMfaResponse.getAttribute("userId"));
    assertThat(enrollVoiceCallMfaResponse.getAttribute("factorId")).isNotNull();
  }

  @Test
  void cannotEnrollVoiceCallMfa_withoutActivatedUser() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder enrollSmsMfaBuilder =
        post(ResourceLinks.USER_ENROLL_VOICE_CALL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_PHONE_MFA_REQUEST)
            .session(session);

    this._mockMvc.perform(enrollSmsMfaBuilder).andExpect(status().is4xxClientError());
  }

  @Test
  void cannotEnrollVoiceCallMfa_withoutValidPhoneNumber() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder activateUserBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder enrollVoiceCallMfaBuilder =
        post(ResourceLinks.USER_ENROLL_VOICE_CALL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content("{\"userInput\":\"555\"}")
            .session(session);

    this._mockMvc.perform(activateUserBuilder).andExpect(status().isOk());

    this._mockMvc.perform(enrollVoiceCallMfaBuilder).andExpect(status().is4xxClientError());
  }

  @Test
  void enrollEmailMfa_isOk() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder activateUserBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder enrollEmailMfaBuilder =
        post(ResourceLinks.USER_ENROLL_EMAIL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_EMAIL_MFA_REQUEST)
            .session(session);

    HttpSession setPasswordResponse =
        this._mockMvc
            .perform(activateUserBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    HttpSession enrollEmailMfaResponse =
        this._mockMvc
            .perform(enrollEmailMfaBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

    assertThat(setPasswordResponse.getAttribute("userId"))
        .isEqualTo(enrollEmailMfaResponse.getAttribute("userId"));
    assertThat(enrollEmailMfaResponse.getAttribute("factorId")).isNotNull();
  }

  @Test
  void cannotEnrollEmailMfa_withoutActivatedUser() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder enrollEmailMfaBuilder =
        post(ResourceLinks.USER_ENROLL_EMAIL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(VALID_ENROLL_EMAIL_MFA_REQUEST)
            .session(session);

    this._mockMvc.perform(enrollEmailMfaBuilder).andExpect(status().is4xxClientError());
  }

  @Test
  void cannotEnrollEmailMfa_withoutValidEmail() throws Exception {
    MockHttpSession session = new MockHttpSession();

    MockHttpServletRequestBuilder activateUserBuilder =
        post(ResourceLinks.USER_SET_PASSWORD)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("X-Forwarded-For", "1.1.1.1")
            .header("User-Agent", "Chrome")
            .content(VALID_PASSWORD_REQUEST)
            .session(session);

    MockHttpServletRequestBuilder enrollEmailMfaBuilder =
        post(ResourceLinks.USER_ENROLL_EMAIL_MFA)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content("{\"userInput\":\"bademail.com\"}")
            .session(session);

    this._mockMvc.perform(activateUserBuilder).andExpect(status().isOk());

    this._mockMvc.perform(enrollEmailMfaBuilder).andExpect(status().is4xxClientError());
  }
}
