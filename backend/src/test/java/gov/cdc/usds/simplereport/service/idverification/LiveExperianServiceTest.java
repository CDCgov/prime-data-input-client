package gov.cdc.usds.simplereport.service.idverification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsResponse;
import gov.cdc.usds.simplereport.properties.ExperianProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class LiveExperianServiceTest {

  private LiveExperianService _service;

  private final RestTemplate _mockRestTemplate = mock(RestTemplate.class);

  private static final ObjectMapper _mapper = new ObjectMapper();
  private static final ExperianProperties FAKE_PROPERTIES =
      new ExperianProperties(
          "http://localhost/experian/fake/token",
          "http://localhost/experian/fake/endpoint",
          "example.com",
          "client_id_value",
          "client_secret_value",
          "subscriber_subcode",
          "cc_username",
          "cc_password",
          "pid_tenant_id",
          "pid_client_reference_id",
          "pid_username",
          "pid_password");
  private static final String EXPERIAN_QUESTIONS_SAMPLE_RESPONSE =
      "{\"responseHeader\":{\"requestType\":\"PreciseIdOnly\",\"clientReferenceId\":\"\",\"expRequestId\":\"\",\"messageTime\":\"2021-07-14T13:09:24Z\",\"overallResponse\":{\"decision\":\"REFER\",\"decisionText\":\"Continue & Investigate\",\"decisionReasons\":[\"Continue & Investigate\"],\"recommendedNextActions\":[],\"spareObjects\":[]},\"responseCode\":\"R0201\",\"responseType\":\"INFO\",\"responseMessage\":\"Workflow Complete.\",\"tenantID\":\"\"},\"clientResponsePayload\":{\"orchestrationDecisions\":[{\"sequenceId\":\"1\",\"decisionSource\":\"PreciseId\",\"decision\":\"REFER\",\"decisionReasons\":[\"Continue & Investigate\"],\"score\":677,\"decisionText\":\"Continue & Investigate\",\"nextAction\":\"Continue\",\"appReference\":\"2173376947\",\"decisionTime\":\"2021-07-14T17:09:26Z\"}],\"decisionElements\":[{\"serviceName\":\"PreciseId\",\"applicantId\":\"APPLICANT_CONTACT_ID_1\",\"decision\":\"R10\",\"score\":677,\"decisionText\":\"Refer\",\"appReference\":\"2173376947\",\"rules\":[{\"ruleId\":\"3201\",\"ruleName\":\"glbRule01\",\"ruleText\":\"Incoming Application Omits Best Address on File\"},{\"ruleId\":\"3402\",\"ruleName\":\"glbRule02\",\"ruleText\":\"Additional Addresses (1 - 2)\"},{\"ruleId\":\"3405\",\"ruleName\":\"glbRule03\",\"ruleText\":\"Additional Addresses (4 - 5)\"}],\"otherData\":{\"json\":{\"fraudSolutions\":{\"response\":{\"products\":{\"preciseIDServer\":{\"sessionID\":\"SAMPLE_SESSION_ID\",\"header\":{\"reportDate\":\"07142021\",\"reportTime\":\"120926\",\"productOption\":\"24\",\"subcode\":\"2926694\",\"referenceNumber\":\"TEST-513-462\"},\"messages\":{\"message\":[{\"number\":\"57\",\"text\":\"015000    0900\",\"addrMismatch\":\"N\"}],\"consumerStatement\":[]},\"summary\":{\"transactionID\":\"2173376947\",\"initialDecision\":\"R10\",\"finalDecision\":\"R10\",\"scores\":{\"preciseIDScore\":\"677\",\"preciseIDScorecard\":\"AC OPEN V2\",\"validationScore\":\"000568\",\"validationScorecard\":\"AC OPEN VAL V2\",\"verificationScore\":\"000686\",\"verificationScorecard\":\"AC OPEN ID THEFT V2\",\"complianceDescription\":\"No Compliance Code\",\"reasons\":{\"reason\":[{\"value\":\"Low level of authentication indicative of First Party Fraud\",\"code\":\"B201\"},{\"value\":\"High average credit limit or loan amount on revolving/real property trades or credit balance to limit ratio on revolving trades\",\"code\":\"B109\"},{\"value\":\"Lack of public record information or collection trades indicative that file is susceptible to ID fraud\",\"code\":\"B110\"},{\"value\":\"A high number of bankcard, installment or real property trades never reported delinquent or derogatory indicative that file is susceptible to ID fraud\",\"code\":\"B108\"},{\"value\":\"No adverse factor observed\",\"code\":\"B405\"}]},\"fpdscore\":\"000655\",\"fpdscorecard\":\"AC OPEN FPD V2\"}},\"preciseMatch\":{\"version\":\"02.00\",\"responseStatusCode\":{\"value\":\"Data found for search request\",\"code\":\"00\"},\"preciseMatchTransactionID\":\"59e7faf5-491d-4ef0-9\",\"preciseMatchScore\":\"488\",\"preciseMatchDecision\":{\"value\":\"\",\"code\":\" \"},\"addresses\":{\"address\":[{\"summary\":{\"verificationResult\":{\"value\":\"Exact match on first and last name;No match to street name (all other fields match)\",\"code\":\"A3\"},\"type\":{\"value\":\"No information available\",\"code\":\"N \"},\"unitMismatchResult\":{\"value\":\"\",\"code\":\"  \"},\"highRiskResult\":{\"value\":\"No address high risk information found\",\"code\":\"N \"},\"counts\":{\"standardizedAddressReturnCount\":0,\"residentialAddressMatchCount\":1,\"residentialAddressReturnCount\":1,\"highRiskAddressReturnCount\":0,\"businessAddressMatchCount\":0,\"businessAddressReturnCount\":0}},\"detail\":{\"residentialAddressRcd\":[{\"surname\":\"KIDD\",\"firstName\":\"BONNIE\",\"middle\":\"K\",\"aliasName\":[],\"address\":\"8816 W 124 ST\",\"city\":\"OVERLAND PARK\",\"state\":\"KS\",\"zipCode\":\"66213\",\"areaCode\":\"913\",\"phone\":\"7644215\"}],\"highRiskAddressRcd\":[],\"highRiskAddressDescription\":[{\"highRiskDescription\":\"No high risk business at address/phone\"}],\"businessAddressRcd\":[]}}]},\"phones\":{\"phone\":[{\"summary\":{\"verificationResult\":{\"value\":\"Phone Number not Supplied\",\"code\":\"MX\"},\"classification\":{\"value\":\"No Phone Classification Found\",\"code\":\"X\"},\"highRiskResult\":{\"value\":\"No phone high risk information found\",\"code\":\"N\"},\"counts\":{\"residentialPhoneMatchCount\":0,\"residentialPhoneReturnCount\":0,\"highRiskPhoneReturnCount\":0,\"businessPhoneMatchCount\":0,\"businessPhoneReturnCount\":0}},\"detail\":{\"residentialPhoneRcd\":[],\"phoneHighRiskRcd\":[],\"highRiskPhoneDescription\":[{\"highRiskDescription\":\"No high risk business at address/phone\"}],\"businessPhoneRcd\":[]}}]},\"consumerID\":{\"summary\":{\"verificationResult\":{\"value\":\"SSN is missing;no match found using name/address search\",\"code\":\"M \"},\"deceasedResult\":{\"value\":\"SSN not provided or validated\",\"code\":\" \"},\"formatResult\":{\"value\":\"SSN not provided or validated\",\"code\":\" \"},\"issueResult\":{\"value\":\"SSN not provided or validated\",\"code\":\" \"},\"counts\":{\"consumerIDReturnCount\":0}}},\"dateOfBirth\":{\"summary\":{\"matchResult\":{\"value\":\"SSN/SSN name and address not on file; verification cannot be done\",\"code\":\"5\"}}},\"driverLicense\":{\"summary\":{\"verificationResult\":{\"value\":\"\",\"code\":\"\"},\"formatValidation\":{\"value\":\"Driver's license not provided or validated\",\"code\":\" \"}}},\"changeOfAddresses\":{\"changeOfAddress\":[{\"summary\":{\"verificationResult\":{\"value\":\"No change of address information found\",\"code\":\"N \"},\"counts\":{\"changeOfAddressReturnCount\":0}}}]},\"ofac\":{\"summary\":{\"verificationResult\":{\"value\":\"\",\"code\":\"  \"},\"counts\":{\"ofacReturnCount\":0}}},\"previousAddresses\":{\"previousAddress\":[{\"summary\":{\"counts\":{\"previousAddressReturnCount\":0}}}]},\"ssnfinder\":{\"summary\":{\"counts\":{\"ssnfinderReturnCount\":0}}}},\"kba\":{\"general\":{\"sessionID\":\"SAMPLE_SESSION_ID\",\"numberOfQuestions\":4,\"kbaresultCode\":0,\"kbaresultCodeDescription\":\"KBA processing successful questions returned\"},\"questionSet\":[{\"questionType\":23,\"questionText\":\"Which of the following is a current or previous employer? If there is not a matched employer name, please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"BIG LOTS\",\"MEDICAL EXPRESS\",\"OUTDOOR SALES SURPLUS\",\"SPILINHOSP\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":42,\"questionText\":\"Using your date of birth, please select your astrological sun sign of the zodiac from the following choices.\",\"questionSelect\":{\"questionChoice\":[\"PISCES\",\"LIBRA\",\"TAURUS\",\"LEO\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":25,\"questionText\":\"Which of the following businesses have you been associated with? If there is not a matched business name, please select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"ALEX\",\"FULTIME STUDENT\",\"MEGA PRINTERS\",\"CHUCK E CHEESE\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":1,\"questionText\":\"According to your credit profile, you may have opened a mortgage loan in or around May 2017. Please select the lender to whom you currently make your mortgage payments. If you do not have a mortgage, select 'NONE OF THE ABOVE/DOES NOT APPLY'.\",\"questionSelect\":{\"questionChoice\":[\"MORTGAGE COMPANIES\",\"SUN WEST MTG\",\"PNC MORTGAGE\",\"GMAC MORTGAGE\",\"NONE OF THE ABOVE/DOES NOT APPLY\"]}}],\"creditQuestionSet\":[],\"nonCreditQuestionSet\":[]},\"pidxmlversion\":\"06.00\",\"fcraDetail\":{\"fraudShield\":{\"indicator\":[{\"value\":\"N\",\"code\":\"07\"},{\"value\":\"N\",\"code\":\"08\"},{\"value\":\"N\",\"code\":\"09\"},{\"value\":\"N\",\"code\":\"19\"},{\"value\":\"N\",\"code\":\"20\"},{\"value\":\"N\",\"code\":\"22\"},{\"value\":\"N\",\"code\":\"27\"}]},\"adverseActions\":{\"adverseAction\":[{\"value\":\"\",\"code\":\" \"},{\"value\":\"\",\"code\":\" \"},{\"value\":\"\",\"code\":\" \"},{\"value\":\"\",\"code\":\" \"}]},\"fcrarules\":{\"fcrarule\":[{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"}]}},\"glbDetail\":{\"fraudShield\":{\"indicator\":[{\"value\":\"N\",\"code\":\"01\"},{\"value\":\"N\",\"code\":\"02\"},{\"value\":\"N\",\"code\":\"03\"},{\"value\":\"N\",\"code\":\"04\"},{\"value\":\"N\",\"code\":\"05\"},{\"value\":\"N\",\"code\":\"06\"},{\"value\":\"N\",\"code\":\"10\"},{\"value\":\"N\",\"code\":\"11\"},{\"value\":\"N\",\"code\":\"13\"},{\"value\":\"N\",\"code\":\"14\"},{\"value\":\"N\",\"code\":\"15\"},{\"value\":\"N\",\"code\":\"16\"},{\"value\":\"N\",\"code\":\"17\"},{\"value\":\"N\",\"code\":\"18\"},{\"value\":\"N\",\"code\":\"21\"},{\"value\":\"N\",\"code\":\"25\"},{\"value\":\"N\",\"code\":\"26\"}]},\"glbRules\":{\"glbRule\":[{\"value\":\"Incoming Application Omits Best Address on File\",\"code\":\"3201\"},{\"value\":\"Additional Addresses (1 - 2)\",\"code\":\"3402\"},{\"value\":\"Additional Addresses (4 - 5)\",\"code\":\"3405\"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"},{\"value\":\"\",\"code\":\"    \"}]}}}}}}}},\"decisions\":[{\"element\":\"messageNumber\",\"value\":\"57\",\"reason\":\"015000    0900\"},{\"element\":\"initialDecision\",\"value\":\"R10\",\"reason\":\"\"},{\"element\":\"finalDecision\",\"value\":\"R10\",\"reason\":\"\"},{\"element\":\"kbaResultCode\",\"value\":\"0\",\"reason\":\"KBA processing successful questions returned\"},{\"element\":\"pidScoreReason1\",\"value\":\"B201\",\"reason\":\"Low level of authentication indicative of First Party Fraud\"},{\"element\":\"pidScoreReason2\",\"value\":\"B109\",\"reason\":\"High average credit limit or loan amount on revolving/real property trades or credit balance to limit ratio on revolving trades\"},{\"element\":\"pidScoreReason3\",\"value\":\"B110\",\"reason\":\"Lack of public record information or collection trades indicative that file is susceptible to ID fraud\"},{\"element\":\"pidScoreReason4\",\"value\":\"B108\",\"reason\":\"A high number of bankcard, installment or real property trades never reported delinquent or derogatory indicative that file is susceptible to ID fraud\"},{\"element\":\"pidScoreReason5\",\"value\":\"B405\",\"reason\":\"No adverse factor observed\"}],\"matches\":[{\"name\":\"pmAddressVerificationResult1\",\"value\":\"A3\"},{\"name\":\"pmPhoneVerificationResult1\",\"value\":\"MX\"},{\"name\":\"pmConsumerIDVerificationResult\",\"value\":\"M\"},{\"name\":\"pmDateOfBirthMatchResult\",\"value\":\"5\"},{\"name\":\"pmDriverLicenseVerificationResult\",\"value\":\"\"},{\"name\":\"pmChangeOfAddressVerificationResult1\",\"value\":\"N\"},{\"name\":\"pmOFACVerificationResult\",\"value\":\"\"},{\"name\":\"fcraFSIndicator07\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator08\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator09\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator19\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator20\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator22\",\"value\":\"N\"},{\"name\":\"fcraFSIndicator27\",\"value\":\"N\"},{\"name\":\"glbFSIndicator01\",\"value\":\"N\"},{\"name\":\"glbFSIndicator02\",\"value\":\"N\"},{\"name\":\"glbFSIndicator03\",\"value\":\"N\"},{\"name\":\"glbFSIndicator04\",\"value\":\"N\"},{\"name\":\"glbFSIndicator05\",\"value\":\"N\"},{\"name\":\"glbFSIndicator06\",\"value\":\"N\"},{\"name\":\"glbFSIndicator10\",\"value\":\"N\"},{\"name\":\"glbFSIndicator11\",\"value\":\"N\"},{\"name\":\"glbFSIndicator13\",\"value\":\"N\"},{\"name\":\"glbFSIndicator14\",\"value\":\"N\"},{\"name\":\"glbFSIndicator15\",\"value\":\"N\"},{\"name\":\"glbFSIndicator16\",\"value\":\"N\"},{\"name\":\"glbFSIndicator17\",\"value\":\"N\"},{\"name\":\"glbFSIndicator18\",\"value\":\"N\"},{\"name\":\"glbFSIndicator21\",\"value\":\"N\"},{\"name\":\"glbFSIndicator25\",\"value\":\"N\"},{\"name\":\"glbFSIndicator26\",\"value\":\"N\"}],\"scores\":[{\"name\":\"preciseIDScore\",\"score\":677,\"type\":\"score\"},{\"name\":\"validationScore\",\"score\":568,\"type\":\"score\"},{\"name\":\"verificationScore\",\"score\":686,\"type\":\"score\"},{\"name\":\"fpdScore\",\"score\":655,\"type\":\"score\"},{\"name\":\"preciseMatchScore\",\"score\":488,\"type\":\"score\"}]}]},\"originalRequestData\":{\"control\":[{\"option\":\"PIDXML_VERSION\",\"value\":\"06.00\"},{\"option\":\"SUBSCRIBER_PREAMBLE\",\"value\":\"TBD3\"},{\"option\":\"SUBSCRIBER_OPERATOR_INITIAL\",\"value\":\"CD\"},{\"option\":\"SUBSCRIBER_SUB_CODE\",\"value\":\"2926694\"},{\"option\":\"PID_USERNAME\",\"value\":\"cdcusds_demo\"},{\"option\":\"PID_PASSWORD\",\"value\":\"dFkwU09FQlZqM0M4RjJFWEdQN3FJS2RvZnpCZzdL\"},{\"option\":\"VERBOSE\",\"value\":\"Y\"},{\"option\":\"PRODUCT_OPTION\",\"value\":\"24\"},{\"option\":\"DETAIL_REQUEST\",\"value\":\"D\"},{\"option\":\"VENDOR\",\"value\":\"123\"},{\"option\":\"VENDOR_VERSION\",\"value\":\"11\"},{\"option\":\"BROKER_NUMBER\",\"value\":\"\"},{\"option\":\"END_USER\",\"value\":\"\"},{\"option\":\"FREEZE_KEY_PIN\",\"value\":\"\"}],\"contacts\":[{\"id\":\"APPLICANT_CONTACT_ID_1\",\"person\":{\"personDetails\":{\"dateOfBirth\":\"1965-08-21\"},\"names\":[{\"firstName\":\"BONNIE\",\"middleNames\":\"K\",\"surName\":\"KIDD\"}]},\"addresses\":[{\"id\":\"Main_Contact_Address_0\",\"addressType\":\"CURRENT\",\"poBoxNumber\":\"\",\"street\":\"8816 W 49 WEST 124TH ST\",\"street2\":\"\",\"postTown\":\"OVERLAND PARK\",\"postal\":\"66213\",\"stateProvinceCode\":\"KS\"}]}],\"application\":{\"productDetails\":{\"productType\":\"WRITTEN_INSTRUCTIONS\"},\"applicants\":[{\"contactId\":\"APPLICANT_CONTACT_ID_1\",\"applicantType\":\"CO_APPLICANT\"}]}}}";
  private static final String SAMPLE_SESSION_ID = "SAMPLE_SESSION_ID";

  @BeforeEach
  public void setup() throws JsonProcessingException {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode fakeTokenResponse = factory.objectNode();
    fakeTokenResponse.put("access_token", "fake_token_value");

    when(_mockRestTemplate.postForObject(
            eq(FAKE_PROPERTIES.getTokenEndpoint()), any(), eq(ObjectNode.class)))
        .thenReturn(fakeTokenResponse);

    ObjectNode questionsSampleResponse =
        _mapper.readValue(EXPERIAN_QUESTIONS_SAMPLE_RESPONSE, ObjectNode.class);
    when(_mockRestTemplate.postForObject(
            eq(FAKE_PROPERTIES.getInitialRequestEndpoint()), any(), eq(ObjectNode.class)))
        .thenReturn(questionsSampleResponse);

    _service = new LiveExperianService(FAKE_PROPERTIES, _mockRestTemplate);
  }

  @Test
  void getQuestions_success() {
    IdentityVerificationQuestionsRequest request = createValidQuestionsRequest();
    IdentityVerificationQuestionsResponse response = _service.getQuestions(request);

    assertTrue(response.getQuestionSet().size() > 0);
    assertTrue(response.getQuestionSet().get(0).has("questionType"));
    assertTrue(response.getQuestionSet().get(0).has("questionText"));
    assertTrue(response.getQuestionSet().get(0).has("questionSelect"));

    assertEquals(SAMPLE_SESSION_ID, response.getSessionId());
  }

  // test

  private IdentityVerificationQuestionsRequest createValidQuestionsRequest() {
    IdentityVerificationQuestionsRequest request = new IdentityVerificationQuestionsRequest();
    request.setFirstName("First");
    request.setLastName("Last");
    request.setDateOfBirth("1945-06-07");
    request.setEmail("test@user.com");
    request.setPhoneNumber("800-555-1212");
    request.setStreetAddress1("1234 Main St.");
    request.setCity("Any City");
    request.setState("CA");
    request.setZip("90210");
    return request;
  }
}
