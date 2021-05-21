package gov.cdc.usds.simplereport.api.model.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(
    code = HttpStatus.FORBIDDEN,
    reason =
        "Okta does not permit this operation.")
public class OktaAuthenticationFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OktaAuthenticationFailureException(String message) {
        super(message);
    }
}
