package gov.cdc.usds.simplereport.api.model.accountrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityVerificationQuestionsRequest {
  @JsonProperty @NotNull private String firstName;
  @JsonProperty @NotNull private String lastName;
  @JsonProperty private String middleName;
  @JsonProperty @NotNull private String dateOfBirth;
  @JsonProperty @NotNull private String email;
  @JsonProperty @NotNull private String phoneNumber;
  @JsonProperty @NotNull private String streetAddress1;
  @JsonProperty private String streetAddress2;
  @JsonProperty @NotNull private String city;
  @JsonProperty @NotNull private String state;
  @JsonProperty @NotNull private String zip;
  @JsonProperty private String poBoxNumber;
}
