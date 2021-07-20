package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;
import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createSubmitAnswersRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationQuestionsResponse;
import gov.cdc.usds.simplereport.api.model.errors.BadRequestException;
import gov.cdc.usds.simplereport.properties.ExperianProperties;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty("simple-report.experian.enabled")
public class LiveExperianService
    implements gov.cdc.usds.simplereport.service.idverification.ExperianService {

  private static final String SUCCESS_DECISION = "ACCEPT";
  private final ExperianProperties _experianProperties;
  private RestTemplate _restTemplate;

  @Autowired
  public LiveExperianService(final ExperianProperties experianProperties) {
    _experianProperties = experianProperties;
    _restTemplate = new RestTemplate();
  }

  public LiveExperianService(
      final ExperianProperties experianProperties, final RestTemplate restTemplate) {
    _experianProperties = experianProperties;
    _restTemplate = restTemplate;
  }

  private String fetchToken() {
    String guid = UUID.randomUUID().toString();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("X-Correlation-Id", guid);
    headers.add("X-User-Domain", _experianProperties.getDomain());

    final JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode requestBody = factory.objectNode();
    requestBody.put("username", _experianProperties.getCrosscoreUsername());
    requestBody.put("password", _experianProperties.getCrosscorePassword());
    requestBody.put("client_id", _experianProperties.getClientId());
    requestBody.put("client_secret", _experianProperties.getClientSecret());

    HttpEntity<ObjectNode> entity = new HttpEntity<>(requestBody, headers);
    try {
      ObjectNode responseBody =
          _restTemplate.postForObject(
              _experianProperties.getTokenEndpoint(), entity, ObjectNode.class);
      if (responseBody == null) {
        throw new RestClientException("The Experian token request returned a null response.");
      }
      return responseBody.path("access_token").asText();
    } catch (RestClientException e) {
      throw new IllegalArgumentException("The activation token could not be retrieved: ", e);
    }
  }

  public IdentityVerificationQuestionsResponse getQuestions(
      IdentityVerificationQuestionsRequest userData) {
    try {
      ObjectNode initialRequestBody =
          createInitialRequestBody(
              _experianProperties.getCrosscoreSubscriberSubcode(),
              _experianProperties.getPreciseidUsername(),
              _experianProperties.getPreciseidPassword(),
              _experianProperties.getPreciseidTenantId(),
              _experianProperties.getPreciseidClientReferenceId(),
              userData);
      ObjectNode responseEntity = submitExperianRequest(initialRequestBody);

      int kbaResultCode =
          responseEntity
              .at(
                  "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kbascore/general/kbaresultCode")
              .asInt();
      if (kbaResultCode == 9) {
        String kbaResultCodeDescription =
            responseEntity
                .at(
                    "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kbascore/general/kbaresultCodeDescription")
                .asText();
        throw new BadRequestException(kbaResultCodeDescription);
      }

      JsonNode questionsDataNode =
          responseEntity.at(
              "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kba/questionSet");
      String sessionId =
          responseEntity
              .at(
                  "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kba/general/sessionID")
              .textValue();

      return new IdentityVerificationQuestionsResponse(sessionId, questionsDataNode);
    } catch (RestClientException | JsonProcessingException e) {
      throw new IllegalStateException("Questions could not be retrieved from Experian: ", e);
    }
  }

  public IdentityVerificationAnswersResponse submitAnswers(
      IdentityVerificationAnswersRequest answersRequest) {
    try {
      ObjectNode finalRequestBody =
          createSubmitAnswersRequestBody(
              _experianProperties.getCrosscoreSubscriberSubcode(),
              _experianProperties.getPreciseidUsername(),
              _experianProperties.getPreciseidPassword(),
              _experianProperties.getPreciseidTenantId(),
              _experianProperties.getPreciseidClientReferenceId(),
              answersRequest);
      ObjectNode responseEntity = submitExperianRequest(finalRequestBody);

      String decision = responseEntity.at("/responseHeader/overallResponse/decision").textValue();

      // if experian responds with ACCEPT, we will consider the id verification successful
      boolean passed = SUCCESS_DECISION.equals(decision);

      return new IdentityVerificationAnswersResponse(passed);
    } catch (RestClientException | JsonProcessingException e) {
      throw new IllegalStateException("Answers could not be validated by Experian: ", e);
    }
  }

  private ObjectNode submitExperianRequest(ObjectNode requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(fetchToken());
    HttpEntity<ObjectNode> entity = new HttpEntity<>(requestBody, headers);
    ObjectNode responseBody =
        _restTemplate.postForObject(
            _experianProperties.getInitialRequestEndpoint(), entity, ObjectNode.class);
    if (responseBody == null) {
      throw new RestClientException("A request to experian returned a null response.");
    }
    return responseBody;
  }
}
