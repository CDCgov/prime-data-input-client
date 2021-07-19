package gov.cdc.usds.simplereport.api.accountrequest;

import static gov.cdc.usds.simplereport.config.AuthorizationConfiguration.AUTHORIZER_BEAN;
import static gov.cdc.usds.simplereport.config.WebConfiguration.IDENTITY_VERIFICATION;

import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsResponse;
import gov.cdc.usds.simplereport.service.idverification.ExperianService;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller used for identity verification via Experian. Note that this controller is
 * automatically authorized.
 */
@PreAuthorize("@" + AUTHORIZER_BEAN + ".permitAllAccountRequests()")
@RestController
@RequestMapping(IDENTITY_VERIFICATION)
public class IdentityVerificationController {

  @Autowired private ExperianService _experianService;

  private static final Logger LOG = LoggerFactory.getLogger(IdentityVerificationController.class);

  @PostConstruct
  private void init() {
    LOG.info("WIP: Identity verification REST endpoint enabled.");
  }

  @PostMapping("/get-questions")
  public IdentityVerificationQuestionsResponse getQuestions(
      HttpServletRequest request,
      @Valid @RequestBody IdentityVerificationQuestionsRequest requestBody) {
    return _experianService.getQuestions(requestBody);
  }

  @PostMapping("/submit-answers")
  public IdentityVerificationAnswersResponse submitAnswers(
      HttpServletRequest request,
      @Valid @RequestBody IdentityVerificationAnswersRequest requestBody) {
    /**
     * example request body: {"answers":["1","2","3","4","5"]} where "1" represents the user
     * selecting "2002" for "Please select the model year of the vehicle you purchased or leased
     * prior to January 2011"
     */
    return _experianService.submitAnswers(requestBody);
  }
}
