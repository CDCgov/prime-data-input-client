package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationAnswersResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
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
public class LiveExperianService implements ExperianService {

  private final ExperianProperties _experianProperties;
  private RestTemplate _restTemplate;

  @Autowired
  public LiveExperianService(final ExperianProperties experianProperties) {
    _experianProperties = experianProperties;
    _restTemplate = new RestTemplate();
  }

  public String fetchToken() {
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
      ObjectNode responseObject =
          _restTemplate.postForObject(
              _experianProperties.getTokenEndpoint(), entity, ObjectNode.class);
      return responseObject.path("access_token").asText();
    } catch (RestClientException | NullPointerException e) {
      throw new IllegalArgumentException("The activation token could not be retrieved: ", e);
    }
  }

  public JsonNode getQuestions(IdentityVerificationRequest userData) {
    try {
      ObjectNode initialRequestBody =
          createInitialRequestBody(
              _experianProperties.getCrosscoreSubscriberSubcode(),
              _experianProperties.getPreciseidUsername(),
              _experianProperties.getPreciseidPassword(),
              _experianProperties.getPreciseidTenantId(),
              _experianProperties.getPreciseidClientReferenceId(),
              userData);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(fetchToken());
      HttpEntity<ObjectNode> entity = new HttpEntity<>(initialRequestBody, headers);
      ObjectNode responseEntity =
          _restTemplate.postForObject(
              _experianProperties.getInitialRequestEndpoint(), entity, ObjectNode.class);
      JsonNode questionsDataNode =
          responseEntity.at(
              "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kba/questionSet");
      String sessionId =
          responseEntity
              .at(
                  "/clientResponsePayload/decisionElements/0/otherData/json/fraudSolutions/response/products/preciseIDServer/kba/general/sessionID")
              .textValue();

      final JsonNodeFactory factory = JsonNodeFactory.instance;
      ObjectNode questionsResponse = factory.objectNode();
      questionsResponse.set("questionSet", questionsDataNode);
      questionsResponse.put("sessionId", sessionId);
      return questionsResponse;
    } catch (RestClientException | NullPointerException | JsonProcessingException e) {
      throw new IllegalStateException("Questions could not be retrieved from Experian: ", e);
    }
  }

  public IdentityVerificationAnswersResponse submitAnswers(
      IdentityVerificationAnswersRequest answerRequest) {
    return new IdentityVerificationAnswersResponse(false);
  }
}
