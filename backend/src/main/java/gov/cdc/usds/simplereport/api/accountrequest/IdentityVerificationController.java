package gov.cdc.usds.simplereport.api.accountrequest;

import static gov.cdc.usds.simplereport.config.WebConfiguration.IDENTITY_VERIFICATION;

import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import gov.cdc.usds.simplereport.service.idVerification.ExperianService;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller used for identity verification via Experian. */
@RestController
@RequestMapping(IDENTITY_VERIFICATION)
public class IdentityVerificationController {
  private final ExperianService _experianService;

  private static final Logger LOG = LoggerFactory.getLogger(IdentityVerificationController.class);

  public IdentityVerificationController(ExperianService experianService) {
    this._experianService = experianService;
  }

  @PostConstruct
  private void init() {
    LOG.info("WIP: Identity verification REST endpoint enabled.");
  }

  @PostMapping("/get-questions")
  public String getQuestions(
      HttpServletRequest request, @RequestBody IdentityVerificationRequest requestBody) {
    String questions = _experianService.getQuestions(requestBody);
    return questions;
  }

  @PostMapping("/submit-answers")
  public String submitAnswers(HttpServletRequest request) {
    /**
     * example request body: {"answers":["1","2","3","4","5"]} where "1" represents the user
     * selecting "2002" for "Please select the model year of the vehicle you purchased or leased
     * prior to January 2011"
     */
    return "{\"passed\":true,\"email\":\"usds@example.com\"}";
  }
}
