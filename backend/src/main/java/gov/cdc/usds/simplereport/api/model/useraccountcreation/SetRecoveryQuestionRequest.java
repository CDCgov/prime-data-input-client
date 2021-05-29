package gov.cdc.usds.simplereport.api.model.useraccountcreation;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetRecoveryQuestionRequest {
  @Size(max = RequestConstants.STANDARD_REQUEST_STRING_LIMIT)
  private String question;

  @Size(max = RequestConstants.STANDARD_REQUEST_STRING_LIMIT)
  private String answer;
}
