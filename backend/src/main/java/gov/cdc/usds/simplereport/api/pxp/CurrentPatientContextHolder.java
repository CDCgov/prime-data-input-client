package gov.cdc.usds.simplereport.api.pxp;

import gov.cdc.usds.simplereport.db.model.PatientLink;
import gov.cdc.usds.simplereport.db.model.TestOrder;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;

@Repository
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentPatientContextHolder {

  private TestOrder _currentLinkedOrder;
  private PatientLink _currentPatientLink;

  public TestOrder getLinkedOrder() {
    return _currentLinkedOrder;
  }

  public PatientLink getPatientLink() {
    return _currentPatientLink;
  }

  public void setLinkedOrder(TestOrder currentLinkedOrder) {
    this._currentLinkedOrder = currentLinkedOrder;
  }

  public void setPatientLink(PatientLink currentPatientLink) {
    this._currentPatientLink = currentPatientLink;
  }
}
