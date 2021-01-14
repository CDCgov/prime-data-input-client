package gov.cdc.usds.simplereport.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import gov.cdc.usds.simplereport.test_util.DbTruncator;
import gov.cdc.usds.simplereport.test_util.TestDataFactory;

/**
 * Base class for service-level integration. Avoids setting up servlet and web
 * security, but sets up service and persistence layer.
 *
 * Note that when service-layer security is configured, it will mysteriously not
 * work in tests because security configuration is attached to a web security
 * configuration module. (That module is also on the wrong profile, so there's a
 * significant rearrangement required regardless.)
 */
@SpringBootTest(properties = {
        "spring.main.web-application-type=NONE",
        "simple-report.authorization.role-prefix=TEST-TENANT:",
})
@ActiveProfiles("dev")
@WithMockUser(authorities = { "TEST-TENANT:DIS_ORG:USER" })
public abstract class BaseServiceTest<T> {

    @Autowired
    private DbTruncator _truncator;
    @Autowired
    private OrganizationInitializingService _initService;
    @Autowired
    protected TestDataFactory _dataFactory;
    @Autowired
    protected T _service;

    @BeforeEach
    public void clearDb() {
        _truncator.truncateAll();
    }

    protected void initSampleData() {
        _initService.initAll();
    }

    protected void reset() {
        _truncator.truncateAll();
    }
}
