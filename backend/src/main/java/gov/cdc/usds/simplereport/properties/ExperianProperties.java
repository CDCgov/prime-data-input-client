package gov.cdc.usds.simplereport.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "simple-report.experian")
@Getter
public class ExperianProperties {

  private final String tokenEndpoint;
  private final String initialRequestEndpoint;
  private final String domain;
  private final String clientId;
  private final String clientSecret;

  private final String crosscoreSubscriberSubcode;
  private final String crosscoreUsername;
  private final String crosscorePassword;

  private final String preciseidTenantId;
  private final String preciseidClientReferenceId;
  private final String preciseidUsername;
  private final String preciseidPassword;

  @ConstructorBinding
  public ExperianProperties(
      String tokenEndpoint,
      String initialRequestEndpoint,
      String domain,
      String clientId,
      String clientSecret,
      String crosscoreSubscriberSubcode,
      String crosscoreUsername,
      String crosscorePassword,
      String preciseidTenantId,
      String preciseidClientReferenceId,
      String preciseidUsername,
      String preciseidPassword) {
    this.tokenEndpoint = tokenEndpoint;
    this.initialRequestEndpoint = initialRequestEndpoint;
    this.domain = domain;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.crosscoreSubscriberSubcode = crosscoreSubscriberSubcode;
    this.crosscoreUsername = crosscoreUsername;
    this.crosscorePassword = crosscorePassword;
    this.preciseidTenantId = preciseidTenantId;
    this.preciseidClientReferenceId = preciseidClientReferenceId;
    this.preciseidUsername = preciseidUsername;
    this.preciseidPassword = preciseidPassword;
  }
}
