package gov.cdc.usds.simplereport.idp.repository;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.config.BeanProfiles;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRole;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRoleClaims;
import gov.cdc.usds.simplereport.config.authorization.PermissionHolder;
import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.service.model.IdentityAttributes;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Handles all user/organization management in Okta */
@Profile(BeanProfiles.NO_OKTA_MGMT)
@Service
public class DemoOktaRepository implements OktaRepository {

  private static final Logger LOG = LoggerFactory.getLogger(DemoOktaRepository.class);

  Map<String, OrganizationRoleClaims> usernameOrgRolesMap;
  Map<String, Set<String>> orgUsernamesMap;
  Map<String, Set<UUID>> orgFacilitiesMap;
  Set<String> inactiveUsernames;

  public DemoOktaRepository() {
    this.usernameOrgRolesMap = new HashMap<>();
    this.orgUsernamesMap = new HashMap<>();
    this.orgFacilitiesMap = new HashMap<>();
    this.inactiveUsernames = new HashSet<>();

    LOG.info("Done initializing Demo Okta repository.");
  }

  public Optional<OrganizationRoleClaims> createUser(
      IdentityAttributes userIdentity,
      Organization org,
      Set<Facility> facilities,
      Set<OrganizationRole> roles) {
    String organizationExternalId = org.getExternalId();
    Set<OrganizationRole> rolesToCreate = EnumSet.of(OrganizationRole.getDefault());
    rolesToCreate.addAll(roles);
    Set<UUID> facilityUUIDs =
        facilities.stream()
            // create an empty set of facilities if user can access all facilities anyway
            .filter(f -> !PermissionHolder.grantsAllFacilityAccess(rolesToCreate))
            .map(f -> f.getInternalId())
            .collect(Collectors.toSet());
    if (!orgFacilitiesMap.containsKey(organizationExternalId)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot add Okta user to nonexistent organization=" + organizationExternalId);
    } else if (!orgFacilitiesMap.get(organizationExternalId).containsAll(facilityUUIDs)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot add Okta user to one or more nonexistent facilities in facilities_set="
              + facilities.stream().map(f -> f.getFacilityName()).collect(Collectors.toSet())
              + " in organization="
              + organizationExternalId);
    }

    OrganizationRoleClaims orgRoles =
        new OrganizationRoleClaims(organizationExternalId, facilityUUIDs, rolesToCreate);
    usernameOrgRolesMap.putIfAbsent(userIdentity.getUsername(), orgRoles);

    orgUsernamesMap.get(organizationExternalId).add(userIdentity.getUsername());

    return Optional.of(orgRoles);
  }

  public Optional<OrganizationRoleClaims> updateUser(
      String oldUsername, IdentityAttributes userIdentity) {
    OrganizationRoleClaims orgRoles = usernameOrgRolesMap.remove(oldUsername);
    usernameOrgRolesMap.put(userIdentity.getUsername(), orgRoles);
    orgUsernamesMap
        .values()
        .forEach(
            usernames -> {
              if (usernames.remove(oldUsername)) {
                usernames.add(userIdentity.getUsername());
              }
            });

    return Optional.of(orgRoles);
  }

  public Optional<OrganizationRoleClaims> updateUserPrivileges(
      String username, Organization org, Set<Facility> facilities, Set<OrganizationRole> roles) {
    String orgId = org.getExternalId();
    if (!orgUsernamesMap.containsKey(orgId)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot update Okta user privileges for nonexistent organization.");
    }
    if (!orgUsernamesMap.get(orgId).contains(username)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot update Okta user privileges for organization they are not in.");
    }
    Set<OrganizationRole> newRoles = EnumSet.of(OrganizationRole.getDefault());
    newRoles.addAll(roles);
    Set<UUID> facilityUUIDs =
        facilities.stream()
            // create an empty set of facilities if user can access all facilities anyway
            .filter(f -> !PermissionHolder.grantsAllFacilityAccess(newRoles))
            .map(f -> f.getInternalId())
            .collect(Collectors.toSet());
    OrganizationRoleClaims newRoleClaims =
        new OrganizationRoleClaims(orgId, facilityUUIDs, newRoles);
    usernameOrgRolesMap.put(username, newRoleClaims);

    return Optional.of(newRoleClaims);
  }

  public void setUserIsActive(String username, Boolean active) {
    if (active) {
      inactiveUsernames.remove(username);
    } else if (!active) {
      inactiveUsernames.add(username);
    }
  }

  public Map<String, OrganizationRoleClaims> getAllUsersForOrganization(Organization org) {
    if (!orgUsernamesMap.containsKey(org.getExternalId())) {
      throw new IllegalGraphqlArgumentException(
          "Cannot get Okta users from nonexistent organization.");
    }
    return orgUsernamesMap.get(org.getExternalId()).stream()
        .filter(u -> !inactiveUsernames.contains(u))
        .collect(Collectors.toMap(u -> u, u -> usernameOrgRolesMap.get(u)));
  }

  // this method dodsn't mean much in a demo env
  public void createOrganization(
      Organization org, Collection<Facility> facilities, boolean migration) {
    createOrganization(org);
    facilities.forEach(f -> createFacility(f));
  }

  public void createOrganization(Organization org) {
    String externalId = org.getExternalId();
    orgUsernamesMap.putIfAbsent(externalId, new HashSet<>());
    orgFacilitiesMap.putIfAbsent(externalId, new HashSet<>());
  }

  public void createFacility(Facility facility) {
    String orgExternalId = facility.getOrganization().getExternalId();
    if (!orgFacilitiesMap.containsKey(orgExternalId)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot create Okta facility in nonexistent organization.");
    }
    orgFacilitiesMap.get(orgExternalId).add(facility.getInternalId());
  }

  public void deleteOrganization(Organization org) {
    String externalId = org.getExternalId();
    orgUsernamesMap.remove(externalId);
    orgFacilitiesMap.remove(externalId);
    // remove all users from this map whose org roles are in the deleted org
    usernameOrgRolesMap =
        usernameOrgRolesMap.entrySet().stream()
            .filter(e -> !(e.getValue().getOrganizationExternalId().equals(externalId)))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }

  public void deleteFacility(Facility facility) {
    String orgExternalId = facility.getOrganization().getExternalId();
    if (!orgFacilitiesMap.containsKey(orgExternalId)) {
      throw new IllegalGraphqlArgumentException(
          "Cannot delete Okta facility from nonexistent organization.");
    }
    orgFacilitiesMap.get(orgExternalId).remove(facility.getInternalId());
    // remove this facility from every user's OrganizationRoleClaims, as necessary
    usernameOrgRolesMap =
        usernameOrgRolesMap.entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey(),
                    e -> {
                      OrganizationRoleClaims oldRoleClaims = e.getValue();
                      Set<UUID> newFacilities =
                          oldRoleClaims.getFacilities().stream()
                              .filter(f -> !f.equals(facility.getInternalId()))
                              .collect(Collectors.toSet());
                      return new OrganizationRoleClaims(
                          orgExternalId, newFacilities, oldRoleClaims.getGrantedRoles());
                    }));
  }

  public Optional<OrganizationRoleClaims> getOrganizationRoleClaimsForUser(String username) {
    if (inactiveUsernames.contains(username)) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(usernameOrgRolesMap.get(username));
    }
  }

  public void reset() {
    usernameOrgRolesMap.clear();
    orgUsernamesMap.clear();
    orgFacilitiesMap.clear();
    inactiveUsernames.clear();
  }
}
