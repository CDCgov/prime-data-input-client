package gov.cdc.usds.simplereport.idp.authentication;

import gov.cdc.usds.simplereport.api.model.errors.InvalidActivationLinkException;
import gov.cdc.usds.simplereport.api.model.errors.OktaAuthenticationFailureException;

/**
 * Created by emmastephenson on 4/28/21
 *
 * <p>Handles all Okta-related authenticaton.
 */
public interface OktaAuthentication {

  public String activateUser(String activationToken, String crossForwardedHeader, String userAgent)
      throws InvalidActivationLinkException;

  public void setPassword(String userId, char[] password) throws OktaAuthenticationFailureException;

  public void setRecoveryQuestion(String userId, String recoveryQuestion, String answer)
      throws OktaAuthenticationFailureException;

  public String enrollSmsMfa(String userId, String phoneNumber)
      throws OktaAuthenticationFailureException;

  public String enrollVoiceCallMfa(String userId, String phoneNumber)
      throws OktaAuthenticationFailureException;

  public String enrollEmailMfa(String userId, String email)
      throws OktaAuthenticationFailureException;
}
