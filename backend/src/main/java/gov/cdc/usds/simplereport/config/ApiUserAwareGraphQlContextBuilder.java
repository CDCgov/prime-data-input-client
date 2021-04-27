package gov.cdc.usds.simplereport.config;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import gov.cdc.usds.simplereport.api.patient.PatientDataResolver;
import gov.cdc.usds.simplereport.config.authorization.ApiUserPrincipal;
import gov.cdc.usds.simplereport.config.authorization.FacilityPrincipal;
import gov.cdc.usds.simplereport.config.authorization.OrganizationPrincipal;
import gov.cdc.usds.simplereport.config.authorization.SiteAdminPrincipal;
import gov.cdc.usds.simplereport.db.model.PatientPreferences;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.repository.PatientPreferencesRepository;
import gov.cdc.usds.simplereport.db.repository.TestEventRepository;
import gov.cdc.usds.simplereport.service.ApiUserService;
import graphql.kickstart.execution.context.DefaultGraphQLContext;
import graphql.kickstart.execution.context.GraphQLContext;
import graphql.kickstart.servlet.context.DefaultGraphQLServletContext;
import graphql.kickstart.servlet.context.DefaultGraphQLWebSocketContext;
import graphql.kickstart.servlet.context.GraphQLServletContextBuilder;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.stereotype.Component;

/**
 * A GraphQL context builder that injects the current API user as the context's subject. The subject
 * is populated with the user's granted permissions, whether the user is a site admin, the user's
 * granted roles, and the organization and facilities to which the user has been granted access.
 */
@Component
class ApiUserAwareGraphQlContextBuilder implements GraphQLServletContextBuilder {
  private final ApiUserService apiUserService;
  private final PatientPreferencesRepository patientPreferencesRepository;
  private final TestEventRepository testEventRepository;

  ApiUserAwareGraphQlContextBuilder(
      ApiUserService apiUserService,
      PatientPreferencesRepository patientPreferencesRepository,
      TestEventRepository testEventRepository) {
    this.apiUserService = apiUserService;
    this.patientPreferencesRepository = patientPreferencesRepository;
    this.testEventRepository = testEventRepository;
  }

  @Override
  public GraphQLContext build(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    return DefaultGraphQLServletContext.createServletContext()
        .with(httpServletRequest)
        .with(httpServletResponse)
        .with(subjectFromCurrentUser())
        .with(buildDataLoaderRegistry())
        .build();
  }

  @Override
  public GraphQLContext build(Session session, HandshakeRequest handshakeRequest) {
    return DefaultGraphQLWebSocketContext.createWebSocketContext()
        .with(session)
        .with(handshakeRequest)
        .with(subjectFromCurrentUser())
        .with(buildDataLoaderRegistry())
        .build();
  }

  @Override
  public GraphQLContext build() {
    return new DefaultGraphQLContext(buildDataLoaderRegistry(), subjectFromCurrentUser());
  }

  private Subject subjectFromCurrentUser() {
    var currentUser = apiUserService.getCurrentUserInfo();
    var principals = new HashSet<Principal>();

    principals.add(new ApiUserPrincipal(currentUser.getWrapped()));

    if (currentUser.getIsAdmin()) {
      principals.add(SiteAdminPrincipal.getInstance());
    }

    principals.addAll(currentUser.getPermissions());
    principals.addAll(currentUser.getRoles());

    currentUser.getOrganization().map(OrganizationPrincipal::new).ifPresent(principals::add);

    currentUser.getFacilities().stream().map(FacilityPrincipal::new).forEach(principals::add);

    return new Subject(true, principals, Collections.emptySet(), Collections.emptySet());
  }

  /**
   * This method does not belong in this class, but this changeset has sprawled too far already and
   * there is <em>already</em> a cleanup/refactor ticket associated with this work.
   */
  private DataLoaderRegistry buildDataLoaderRegistry() {
    DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();

    DataLoader<UUID, PatientPreferences> patientPreferencesDataLoader =
        new DataLoader<>(
            patientIds ->
                supplyAsync(
                    () -> {
                      Map<UUID, PatientPreferences> found =
                          patientPreferencesRepository
                              .findAllByPersonInternalIdIn(patientIds)
                              .stream()
                              .collect(Collectors.toMap(PatientPreferences::getInternalId, s -> s));
                      return patientIds.stream()
                          .map(p -> found.getOrDefault(p, PatientPreferences.DEFAULT))
                          .collect(Collectors.toList());
                    }));
    dataLoaderRegistry.register(PatientPreferences.DATA_LOADER, patientPreferencesDataLoader);

    DataLoader<UUID, TestEvent> testEventDataLoader =
        new DataLoader<>(
            patientIds ->
                supplyAsync(
                    () -> {
                      Map<UUID, TestEvent> found =
                          testEventRepository.findLastTestsByPatient(patientIds).stream()
                              .collect(Collectors.toMap(TestEvent::getPatientInternalID, s -> s));
                      return patientIds.stream()
                          .map(te -> found.getOrDefault(te, null))
                          .collect(Collectors.toList());
                    }));
    dataLoaderRegistry.register(PatientDataResolver.LAST_TEST_DATA_LOADER, testEventDataLoader);

    return dataLoaderRegistry;
  }
}
