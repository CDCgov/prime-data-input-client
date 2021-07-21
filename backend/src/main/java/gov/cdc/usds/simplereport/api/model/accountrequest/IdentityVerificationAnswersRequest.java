package gov.cdc.usds.simplereport.api.model.accountrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityVerificationAnswersRequest {
  @JsonProperty @NotNull private String orgExternalId;
  // experian session id from getting questions
  @JsonProperty @NotNull private String sessionId;
  @JsonProperty @NotNull private List<Integer> answers;
}
