package gov.cdc.usds.simplereport.idp.authentication;

import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.ResourceException;
import com.okta.sdk.resource.user.PasswordCredential;
import com.okta.sdk.resource.user.RecoveryQuestionCredential;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserCredentials;
import com.okta.sdk.resource.user.factor.CallUserFactor;
import com.okta.sdk.resource.user.factor.EmailUserFactor;
import com.okta.sdk.resource.user.factor.SmsUserFactor;
import com.okta.spring.boot.sdk.config.OktaClientProperties;
import gov.cdc.usds.simplereport.api.model.errors.InvalidActivationLinkException;
import gov.cdc.usds.simplereport.api.model.errors.OktaAuthenticationFailureException;
import gov.cdc.usds.simplereport.config.BeanProfiles;
import java.util.List;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by emmastephenson on 4/28/21
 *
 * <p>Handles all Okta-related authenticaton.
 */
@Profile("!" + BeanProfiles.NO_OKTA_AUTH)
@Service
public class LiveOktaAuthentication implements OktaAuthentication {
  private Client _client;
  private String _apiToken;
  private String _orgUrl;

  public LiveOktaAuthentication(OktaClientProperties oktaClientProperties) {
    _client =
        Clients.builder()
            .setOrgUrl(oktaClientProperties.getOrgUrl())
            .setClientCredentials(new TokenClientCredentials(oktaClientProperties.getToken()))
            .build();
    _apiToken = oktaClientProperties.getToken();
    _orgUrl = oktaClientProperties.getOrgUrl();
  }

  /**
   * Converts an activation token into a user id by sending a REST request to the Okta API. (The
   * Okta Authentication SDK does not yet support activating an activation token. If that changes,
   * update this method to use the SDK.) If successful, it moves the Okta state machine into a
   * RESET_PASSWORD state.
   * https://developer.okta.com/docs/reference/api/authn/#request-example-for-activation-token
   * https://developer.okta.com/docs/reference/api/authn/#response-example-for-activation-token-success-user-without-password
   *
   * @param activationToken the token passed from Okta when creating a new user.
   * @param crossForwardedHeader the IP address of the user requesting a new account, along with any
   *     other IP addresses in the request chain (typically Azure).
   * @param userAgent the user agent of the user requesting a new account.
   * @return The user id returned by Okta.
   * @throws OktaAuthenticationFailureException if the state token is not returned by Okta.
   */
  public String activateUser(String activationToken, String crossForwardedHeader, String userAgent)
      throws InvalidActivationLinkException {
    JSONObject requestBody = new JSONObject();
    requestBody.put("token", activationToken);
    String authorizationToken = "SSWS " + _apiToken;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.add(HttpHeaders.USER_AGENT, userAgent);
    headers.add("X-Forwarded-For", crossForwardedHeader);
    headers.add("Authorization", authorizationToken);
    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
    RestTemplate restTemplate = new RestTemplate();
    String postUrl = _orgUrl + "/api/v1/authn";
    try {
      String response = restTemplate.postForObject(postUrl, entity, String.class);
      JSONObject responseJson = new JSONObject(response);
      return responseJson.getJSONObject("_embedded").getJSONObject("user").getString("id");
    } catch (RestClientException | NullPointerException e) {
      throw new InvalidActivationLinkException("User could not be activated", e);
    }
  }

  /**
   * Using the Okta Management SDK, sets a user's password for the first time.
   *
   * @param userId the user id of the user making the request.
   * @param password the user-provided password to set.
   * @throws OktaAuthenticationFailureException if the password doesn't meet Okta requirements or
   *     the user is not in a RESET_PASSWORD state.
   */
  public void setPassword(String userId, char[] password)
      throws OktaAuthenticationFailureException {
    try {
      User user = _client.getUser(userId);
      UserCredentials creds = user.getCredentials();
      PasswordCredential passwordCred =
          _client.instantiate(PasswordCredential.class).setValue(password);
      creds.setPassword(passwordCred);
      user.setCredentials(creds);
      user.update();
    } catch (ResourceException e) {
      throw new OktaAuthenticationFailureException("Error setting user's password", e);
    }
  }

  /**
   * Using the Okta Management SDK, set's a user's recovery questions.
   *
   * @param userId the user id of the user making the request.
   * @param question the user-selected question to answer.
   * @param answer the user-input answer to the selected question.
   * @throws OktaAuthenticationFailureException if the recovery question/answer doesn't meet Okta
   *     requirements.
   */
  public void setRecoveryQuestion(String userId, String question, String answer)
      throws OktaAuthenticationFailureException {
    try {
      User user = _client.getUser(userId);
      UserCredentials creds = user.getCredentials();
      RecoveryQuestionCredential recoveryCred =
          _client
              .instantiate(RecoveryQuestionCredential.class)
              .setQuestion(question)
              .setAnswer(answer);
      creds.setRecoveryQuestion(recoveryCred);
      user.setCredentials(creds);
      user.update();
    } catch (ResourceException e) {
      throw new OktaAuthenticationFailureException("Error setting recovery questions", e);
    }
  }

  /**
   * Using the Okta management SDK, enroll a user in SMS MFA. If successful, this enrollment
   * triggers a text to the user's phone with an activation passcode.
   *
   * @param userId the user id of the user making the request.
   * @param phoneNumber the user-provided phone number to enroll.
   * @return factorId the Okta-generated id for the phone number factor.
   * @throws OktaAuthenticationFailureException if the phone number is invalid or Okta cannot enroll
   *     it as an MFA option.
   */
  public String enrollSmsMfa(String userId, String phoneNumber)
      throws OktaAuthenticationFailureException {
    try {
      SmsUserFactor smsFactor = _client.instantiate(SmsUserFactor.class);
      smsFactor.getProfile().setPhoneNumber(phoneNumber);
      User user = _client.getUser(userId);
      user.enrollFactor(smsFactor);
      return smsFactor.getId();
    } catch (ResourceException e) {
      throw new OktaAuthenticationFailureException("Error setting SMS MFA", e);
    }
  }

  /**
   * Using the Okta management SDK, enroll a user in voice call MFA. If successful, this enrollment
   * triggers a phone call to the user with an activation passcode.
   *
   * @param userId the user id of the user making the enrollment request.
   * @param phoneNumber the user-provided phone number to enroll.
   * @return factorId the Okta-generated id for the voice call factor.
   * @throws OktaAuthenticationFailureException if the phone number is invalid or Okta cannot enroll
   *     it as an MFA option.
   */
  public String enrollVoiceCallMfa(String userId, String phoneNumber)
      throws OktaAuthenticationFailureException {
    try {
      CallUserFactor callFactor = _client.instantiate(CallUserFactor.class);
      callFactor.getProfile().setPhoneNumber(phoneNumber);
      User user = _client.getUser(userId);
      user.enrollFactor(callFactor);
      return callFactor.getId();
    } catch (ResourceException e) {
      throw new OktaAuthenticationFailureException("Error setting voice call MFA", e);
    }
  }

  /**
   * Using the Okta Management SDK, enroll a user in email MFA. If successful, this enrollment
   * triggers an activation email to the user with an OTP.
   *
   * @param userId the user id of the user making the enrollment request.
   * @param email the user-provided email address to enroll. (note: should we do any validation that
   *     this email === the enrolled email?)
   * @throws OktaAuthenticationFailureException if the email is invalid or Okta cannot enroll it as
   *     an MFA option.
   */
  public String enrollEmailMfa(String userId, String email)
      throws OktaAuthenticationFailureException {
    try {
      EmailUserFactor emailFactor = _client.instantiate(EmailUserFactor.class);
      emailFactor.getProfile().setEmail(email);
      User user = _client.getUser(userId);
      user.enrollFactor(emailFactor);
      return emailFactor.getId();
    } catch (ResourceException e) {
      throw new OktaAuthenticationFailureException("Error setting email MFA", e);
    }
  }
}
