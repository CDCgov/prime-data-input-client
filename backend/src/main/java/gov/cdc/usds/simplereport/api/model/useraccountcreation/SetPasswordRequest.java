package gov.cdc.usds.simplereport.api.model.useraccountcreation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SetPasswordRequest {
  @Size(max = RequestConstants.LARGE_REQUEST_STRING_LIMIT)
  @NotNull
  @JsonProperty
  private String password;
}