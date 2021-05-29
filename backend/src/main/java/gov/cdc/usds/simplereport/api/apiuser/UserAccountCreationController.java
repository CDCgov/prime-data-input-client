package gov.cdc.usds.simplereport.api.apiuser;

import static gov.cdc.usds.simplereport.config.WebConfiguration.USER_ACCOUNT_REQUEST;

import gov.cdc.usds.simplereport.api.model.errors.InvalidActivationLinkException;
import gov.cdc.usds.simplereport.api.model.errors.OktaAuthenticationFailureException;
import gov.cdc.usds.simplereport.api.model.useraccountcreation.EnrollMfaRequest;
import gov.cdc.usds.simplereport.api.model.useraccountcreation.SetRecoveryQuestionRequest;
import gov.cdc.usds.simplereport.api.model.useraccountcreation.UserAccountCreationRequest;
import gov.cdc.usds.simplereport.idp.authentication.OktaAuthentication;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller used for user account creation. */
@RestController
@RequestMapping(USER_ACCOUNT_REQUEST)
public class UserAccountCreationController {
  private static final Logger LOG = LoggerFactory.getLogger(UserAccountCreationController.class);

  private static final String USER_ID_KEY = "userId";
  private static final String FACTOR_ID = "factorId";

  @Autowired private OktaAuthentication _oktaAuth;

  @PostConstruct
  private void init() {
    LOG.info(
        "WIP: User account request creation REST endpoint enabled. Not for use in production at this time.");
  }

  /**
   * Validates that the requesting user has been sent an invitation to SimpleReport, ensures the
   * given password meets all requirements, and sets the password in Okta.
   *
   * @param requestBody contains the password
   * @param request contains all header information, including the activation token.
   * @throws InvalidActivationLinkException if the activation token is invalid.
   * @throws OktaAuthenticationFailureException if the password is invalid or if the user is not in
   *     a RESET_PASSWORD state.
   */
  @PostMapping("/initialize-and-set-password")
  public void activateAccountAndSetPassword(
      @RequestBody UserAccountCreationRequest requestBody, HttpServletRequest request)
      throws InvalidActivationLinkException, OktaAuthenticationFailureException {
    String userId =
        _oktaAuth.activateUser(
            requestBody.getActivationToken(),
            request.getHeader("X-Forwarded-For"),
            request.getHeader("User-Agent"));
    if (userId.isEmpty()) {
      throw new OktaAuthenticationFailureException("Returned user id is empty.");
    }
    request.getSession().setAttribute(USER_ID_KEY, userId);
    _oktaAuth.setPassword(userId, requestBody.getPassword().toCharArray());
  }

  /**
   * Sets a recovery question and answer for a user.
   *
   * @param requestBody contains the selected question and user-provided answer
   * @param request contains session information about the user, including their id
   * @throws OktaAuthenticationFailureException if the recovery question or answer don't meet Okta
   *     standards (answers must be at least 4 chars long)
   */
  @PostMapping("/set-recovery-question")
  public void setRecoveryQuestions(
      @RequestBody SetRecoveryQuestionRequest requestBody, HttpServletRequest request)
      throws OktaAuthenticationFailureException {
    String userId = getUserId(request.getSession());
    _oktaAuth.setRecoveryQuestion(userId, requestBody.getQuestion(), requestBody.getAnswer());
  }

  /**
   * Enrolls a user in SMS MFA.
   *
   * @param requestBody contains the user-provided phone number.
   * @param request contains session information about the user, including their Okta id.
   * @throws OktaAuthenticationFailureException if the provided phone number is invalid.
   */
  @PostMapping("/enroll-sms-mfa")
  public void enrollSmsMfa(@RequestBody EnrollMfaRequest requestBody, HttpServletRequest request)
      throws OktaAuthenticationFailureException {
    String userId = getUserId(request.getSession());
    String factorId = _oktaAuth.enrollSmsMfa(userId, requestBody.getUserInput());
    request.getSession().setAttribute(FACTOR_ID, factorId);
  }

  /**
   * Enrolls a user in voice call MFA.
   *
   * @param requestBody contains the user-provided phone number.
   * @param request contains session information about the user, including their Okta id.
   * @throws OktaAuthenticationFailureException if the provided phone number is invalid.
   */
  @PostMapping("/enroll-voice-call-mfa")
  public void enrollVoiceCallMfa(
      @RequestBody EnrollMfaRequest requestBody, HttpServletRequest request)
      throws OktaAuthenticationFailureException {
    String userId = getUserId(request.getSession());
    String factorId = _oktaAuth.enrollVoiceCallMfa(userId, requestBody.getUserInput());
    request.getSession().setAttribute(FACTOR_ID, factorId);
  }

  /**
   * Enrolls a user in email MFA.
   *
   * @param requestBody contains the user-provided email address.
   * @param request contains session information about the user, including their Okta id.
   * @throws OktaAuthenticationFailureException if the provided email address is invalid.
   */
  @PostMapping("/enroll-email-mfa")
  public void enrollEmailMfa(@RequestBody EnrollMfaRequest requestBody, HttpServletRequest request)
      throws OktaAuthenticationFailureException {
    String userId = getUserId(request.getSession());
    String factorId = _oktaAuth.enrollEmailMfa(userId, requestBody.getUserInput());
    request.getSession().setAttribute(FACTOR_ID, factorId);
  }

  /**
   * Begins the enrollment process for authenticator apps.
   *
   * @param requestBody contains the user-selected authentication app to use (for now, one of Google
   *     Authenticator or Okta Verify.)
   * @param request contains session information about the user, including their Okta id.
   * @throws OktaAuthenticationFailureException if Okta cannot enroll the user in MFA.
   */
  @GetMapping("/authenticator-qr")
  public void getAuthQrCode(@RequestBody EnrollMfaRequest requestBody, HttpServletRequest request) {
    // WIP: doesn't interact with Okta yet.
  }

  /**
   * Verifies the passcode sent to a user to complete the MFA enrollment process for SMS, voice
   * call, and authentication app MFA options.
   *
   * @param requestBody contains the user-input passcode to be verified.
   * @param request contains session information about the user, including their Okta id.
   * @throws OktaAuthenticationFailureException if the provided passcode does not match the passcode
   *     sent by Okta.
   */
  @PostMapping("/verify-activation-passcode")
  public void verifyActivationPasscode(
      @RequestBody EnrollMfaRequest requestBody, HttpServletRequest request) {
    // WIP: doesn't interact with Okta yet.
  }

  /**
   * Resends the activation passcode sent to a user (required for MFA enrollment).
   *
   * @param request contains session information about the user, including their Okta id.
   */
  @PostMapping("/resend-activation-passcode")
  public void resendActivationPasscode(HttpServletRequest request) {
    // WIP: doesn't interact with Okta yet.
  }

  private String getUserId(HttpSession session) throws OktaAuthenticationFailureException {
    Object userId = session.getAttribute(USER_ID_KEY);
    if (userId != null) {
      return userId.toString();
    } else {
      throw new OktaAuthenticationFailureException(
          "User id not found; user could not be authenticated.");
    }
  }
}
