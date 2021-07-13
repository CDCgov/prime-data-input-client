package gov.cdc.usds.simplereport.service.idverification;

import static gov.cdc.usds.simplereport.service.idverification.ExperianTranslator.createInitialRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.usds.simplereport.api.model.accountrequest.IdentityVerificationRequest;
import gov.cdc.usds.simplereport.properties.ExperianProperties;
import java.util.UUID;
import org.json.JSONObject;
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
  private final ObjectMapper _objectMapper;
  private RestTemplate _restTemplate;

  @Autowired
  public LiveExperianService(final ExperianProperties experianProperties) {
    _experianProperties = experianProperties;
    _objectMapper = new ObjectMapper();
    _restTemplate = new RestTemplate();
  }

  public String fetchToken() {
    String guid = UUID.randomUUID().toString();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("X-Correlation-Id", guid);
    headers.add("X-User-Domain", _experianProperties.getDomain());

    JSONObject requestBody = new JSONObject();
    requestBody.put("username", _experianProperties.getCrosscoreUsername());
    requestBody.put("password", _experianProperties.getCrosscorePassword());
    requestBody.put("client_id", _experianProperties.getClientId());
    requestBody.put("client_secret", _experianProperties.getClientSecret());
    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
    try {
      String responseEntityStr =
          _restTemplate.postForObject(_experianProperties.getTokenEndpoint(), entity, String.class);
      JsonNode responseRoot = _objectMapper.readTree(responseEntityStr);
      return responseRoot.path("access_token").asText();
    } catch (RestClientException | NullPointerException | JsonProcessingException e) {
      throw new IllegalArgumentException("The activation token could not be retrieved: ", e);
    }
  }

  public String getQuestions(IdentityVerificationRequest userData) {
    String initialRequestBody =
        createInitialRequestBody(
            _experianProperties.getPreciseidUsername(),
            _experianProperties.getPreciseidPassword(),
            userData,
            _experianProperties.getPreciseidTenantId(),
            _experianProperties.getPreciseidClientReferenceId());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(fetchToken());
    HttpEntity<String> entity = new HttpEntity<>(initialRequestBody, headers);
    try {
      String responseEntityStr =
          _restTemplate.postForObject(
              _experianProperties.getInitialRequestEndpoint(), entity, String.class);
      JsonNode responseRoot = _objectMapper.readTree(responseEntityStr);
      JsonNode questions =
          responseRoot.path(
              "clientResponsePayload.decisionElements[0].otherData.json.fraudSolutions.response.products.preciseIDServer.kba.questionSet");
      return questions.asText();
      //      return "{\"questionSet\":[{\"questionType\":28,\"questionText\":\"Please select the
      // model year of the vehicle you purchased or leased prior to January 2011
      // .\",\"questionSelect\":{\"questionChoice\":[\"2002\",\"2003\",\"2004\",\"2005\",\"NONE OF
      // THE ABOVE/DOES NOT APPLY\"]}},{\"questionType\":24,\"questionText\":\"Which of the
      // following professions do you currently or have If there is not a matched profession, please
      // select 'NONE OF THE ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"DENTIST / DENTAL
      // HYGIENIST\",\"SOCIAL WORKER\",\"OPTICIAN / OPTOMETRIST\",\"ELECTRICIAN\",\"NONE OF THE
      // ABOVE/DOES NOT APPLY\"]}},{\"questionType\":41,\"questionText\":\"Please select the number
      // of bedrooms in your home from the following choices. If the number of bedrooms in your home
      // is not one of the choices please select 'NONE OF THE
      // ABOVE'.\",\"questionSelect\":{\"questionChoice\":[\"2\",\"3\",\"4\",\"5\",\"NONE OF THE
      // ABOVE/DOES NOT APPLY\"]}},{\"questionType\":47,\"questionText\":\"According to your credit
      // profile, you may have opened a Home type loan in or around May 2015. Please select the
      // lender to whom you currently made your
      // payments.\",\"questionSelect\":{\"questionChoice\":[\"UC LENDING\",\"CTX MORTGAGE\",\"MID
      // AMERICA MORTGAGE\",\"1ST NATIONWIDE MTG\",\"NONE OF THE ABOVE/DOES NOT
      // APPLY\"]}},{\"questionType\":49,\"questionText\":\"According to our records, you graduated
      // from which of the following High
      // Schools?\",\"questionSelect\":{\"questionChoice\":[\"BAXTER SPRINGS HIGH SCHOOL\",\"BELLS
      // HIGH SCHOOL\",\"LOCKNEY HIGH SCHOOL\",\"AGUA DULCE HIGH SCHOOL\",\"NONE OF THE ABOVE/DOES
      // NOT APPLY\"]}}]}";
      // next steps: unwrap the response and get the real questions
    } catch (RestClientException | NullPointerException | JsonProcessingException e) {
      throw new IllegalStateException("Questions could not be retrieved from Experian: ", e);
    }
  }
}
