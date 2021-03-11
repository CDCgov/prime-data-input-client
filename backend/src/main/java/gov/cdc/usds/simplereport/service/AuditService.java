package gov.cdc.usds.simplereport.service;

import gov.cdc.usds.simplereport.db.model.ApiAuditEvent;
import gov.cdc.usds.simplereport.db.repository.AuditEventRepository;
import gov.cdc.usds.simplereport.logging.GraphqlQueryState;
import gov.cdc.usds.simplereport.service.model.UserInfo;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for recording API access events that must be made available for audits. */
@Service
@Transactional
public class AuditService {

  private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

  private final AuditEventRepository _repo;
  private final ApiUserService _userService;

  public AuditService(AuditEventRepository repo, ApiUserService userService) {
    this._repo = repo;
    this._userService = userService;
  }

  public ApiAuditEvent logGraphQlEvent(GraphqlQueryState state, List<String> errorPaths) {
    LOG.trace("Saving audit event for {}", state.getRequestId());
    UserInfo userInfo = _userService.getCurrentUserInfo();
    return _repo.save(
        new ApiAuditEvent(
            state.getRequestId(),
            state.getHttpDetails(),
            state.getGraphqlDetails(),
            errorPaths,
            userInfo.getWrappedUser(),
            userInfo.getPermissions(),
            userInfo.getIsAdmin(),
            userInfo.getOrganization().orElse(null)));
  }
}
