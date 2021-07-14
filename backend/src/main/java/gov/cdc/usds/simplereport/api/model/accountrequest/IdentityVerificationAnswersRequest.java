package gov.cdc.usds.simplereport.api.model.accountrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
public class IdentityVerificationAnswersRequest {
  @JsonProperty @NotNull private String sessionId;
  @JsonProperty @NotNull private List<Integer> answers;
}
