package gov.cdc.usds.simplereport.logging;

import gov.cdc.usds.simplereport.config.authorization.ApiUserPrincipal;
import gov.cdc.usds.simplereport.config.authorization.OrganizationPrincipal;
import gov.cdc.usds.simplereport.config.authorization.SiteAdminPrincipal;
import gov.cdc.usds.simplereport.config.authorization.UserPermission;
import gov.cdc.usds.simplereport.db.model.auxiliary.GraphQlInputs;
import gov.cdc.usds.simplereport.db.model.auxiliary.HttpRequestDetails;
import gov.cdc.usds.simplereport.service.AuditService;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.kickstart.servlet.context.GraphQLServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLoggingInstrumentation extends SimpleInstrumentation {

  private static final Logger LOG = LoggerFactory.getLogger(AuditLoggingInstrumentation.class);

  private final AuditService _auditService;

  public AuditLoggingInstrumentation(AuditService service) {
    _auditService = service;
  }

  @Override
  public InstrumentationState createState() {
    GraphqlQueryState state = new GraphqlQueryState();
    LOG.trace("Creating state={} for audit", state);
    return state;
  }

  @Override
  @SuppressWarnings("checkstyle:IllegalCatch")
  public InstrumentationContext<ExecutionResult> beginExecution(
      InstrumentationExecutionParameters parameters) {
    String executionId = parameters.getExecutionInput().getExecutionId().toString();
    LOG.trace("Instrumenting query executionId={} for audit", executionId);
    try {
      GraphQLServletContext context = parameters.getContext();
      GraphqlQueryState state = parameters.getInstrumentationState();
      state.setRequestId(executionId);
      state.setHttpDetails(new HttpRequestDetails(context.getHttpServletRequest()));
      state.setGraphqlDetails(
          new GraphQlInputs(
              parameters.getOperation(), parameters.getQuery(), parameters.getVariables()));
      return new ExecutionResultContext(state, context.getSubject().orElseThrow());
    } catch (Exception e) {
      // we don't 100% trust this error not to get swallowed by graphql-java
      LOG.error("Extremely unexpected error creating instrumentation state for audit", e);
      throw e;
    }
  }

  private class /* not static! */ ExecutionResultContext
      extends SimpleInstrumentationContext<ExecutionResult> {

    private final GraphqlQueryState state;
    private final Subject subject;

    public ExecutionResultContext(GraphqlQueryState state, Subject subject) {
      this.state = state;
      this.subject = subject;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void onCompleted(ExecutionResult result, Throwable t) {
      LOG.trace("End of execution, audit entry being saved.");
      List<String> errorPaths =
          result.getErrors().stream()
              .map(GraphQLError::getPath)
              .filter(Objects::nonNull)
              .map(path -> path.stream().map(Object::toString).collect(Collectors.joining("/")))
              .collect(Collectors.toList());
      try {
        _auditService.logGraphQlEvent(
            state,
            errorPaths,
            subject.getPrincipals(ApiUserPrincipal.class).stream()
                .findAny()
                .map(ApiUserPrincipal::getApiUser)
                .orElseThrow(),
            new ArrayList<>(subject.getPrincipals(UserPermission.class)),
            !subject.getPrincipals(SiteAdminPrincipal.class).isEmpty(),
            subject.getPrincipals(OrganizationPrincipal.class).stream()
                .map(OrganizationPrincipal::getOrganization)
                .findAny()
                .orElse(null));
      } catch (Exception e) {
        // we don't 100% trust this error not to get swallowed by graphql-java
        LOG.error("Unexpected error saving audit event", e);
        throw e;
      }
    }
  }
}
