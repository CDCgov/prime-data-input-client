package gov.cdc.usds.simplereport.api.apiuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import gov.cdc.usds.simplereport.api.model.User;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRole;
import gov.cdc.usds.simplereport.config.authorization.UserPermission;
import gov.cdc.usds.simplereport.db.model.ApiUser;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.service.model.OrganizationRoles;
import gov.cdc.usds.simplereport.service.OrganizationService;
import gov.cdc.usds.simplereport.service.ApiUserService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.stereotype.Component;


/**
 * Created by jeremyzitomer-usds on 1/7/21
 */
@Component
public class ApiUserMutationResolver implements GraphQLMutationResolver {

    private final OrganizationService _os;
    private final ApiUserService _us;

    private static final Logger logger = LoggerFactory.getLogger(ApiUserMutationResolver.class);

    public ApiUserMutationResolver(OrganizationService os, ApiUserService us) {
        _os = os;
        _us = us;
    }

    public User addUser(
            String firstName,
            String middleName,
            String lastName,
            String suffix,
            String email,
            String organizationExternalID
                ) {
        _us.assertEmailAvailable(email);
        ApiUser apiUser = _us.createUser(email, firstName, middleName, lastName, suffix, organizationExternalID);
        Optional<Organization> org = _os.getOrganizationForUser(apiUser);
        Boolean isAdmin = _us.isAdmin(apiUser);
        // This assumption doesn't hold for admins, but when creating another user, it's not terribly important yet.
        Optional<OrganizationRoles> orgRoles = org.isPresent() 
                ? Optional.of(new OrganizationRoles(org.get(), Set.of(OrganizationRole.USER)))
                : Optional.empty();
        return new User(apiUser, orgRoles, isAdmin);
    }

    public User updateUser(
            String firstName,
            String middleName,
            String lastName,
            String suffix,
            String newEmail,
            String oldEmail
                ) {
        // if no changes to email are made, email validation will happen inside _us.updateUser()
        if (!newEmail.equals(oldEmail)) {
            _us.assertEmailAvailable(newEmail);
        }
        ApiUser apiUser = _us.updateUser(newEmail, oldEmail, firstName, middleName, lastName, suffix);
        Optional<Organization> org = _os.getOrganizationForUser(apiUser);
        Boolean isAdmin = _us.isAdmin(apiUser);
        // This assumption doesn't hold for admins, but when updating another user, it's not terribly important yet.
        Optional<OrganizationRoles> orgRoles = org.isPresent() 
                ? Optional.of(new OrganizationRoles(org.get(), Set.of(OrganizationRole.USER)))
                : Optional.empty();
        return new User(apiUser, orgRoles, isAdmin);
    }
}



