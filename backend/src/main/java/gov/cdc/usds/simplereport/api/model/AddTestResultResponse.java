package gov.cdc.usds.simplereport.api.model;

import gov.cdc.usds.simplereport.db.model.TestOrder;

public class AddTestResultResponse {
  private TestOrder testOrder;
  private Boolean deliveryStatus;

  public AddTestResultResponse(TestOrder testOrder, Boolean deliveryStatus) {
    this.testOrder = testOrder;
    this.deliveryStatus = deliveryStatus;
  }

  public AddTestResultResponse(TestOrder testOrder) {
    this.testOrder = testOrder;
  }

  public TestOrder getTestResult() {
    return testOrder;
  }

  public Boolean getDeliveryStatus() {
    return deliveryStatus;
  }
}
