package gov.cdc.usds.simplereport.config;

/** Constant-holding class to describe the bean profiles available in this application. */
public final class BeanProfiles {

  /**
   * Profile for testing authentication, authorization, and other server behaviors that need to be
   * verified rather than trusted during development.
   */
  public static final String SERVER_DEBUG = "server-debug";

  /** Profile for mocking and bypassing Okta management API (TEST OR DEMO ONLY) */
  public static final String NO_OKTA_MGMT = "no-okta-mgmt";
  /** Profile for mocking and bypassing Okta authentication API (TEST OR DEMO ONLY) */
  public static final String NO_OKTA_AUTH = "no-okta-auth";
  /** Profile for mocking and bypassing most security (TEST OR DEMO ONLY) */
  public static final String NO_SECURITY = "no-security";
  /** Profile for local development, sometimes also used for QA/Demo environments. */
  public static final String DEVELOPMENT = "dev";
  /** Profile that creates sample data on application startup. */
  public static final String CREATE_SAMPLE_DATA = "create-sample-data";

  private BeanProfiles() {
    /* no instances! */
  }
}
