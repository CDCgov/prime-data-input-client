package gov.cdc.usds.simplereport.api;

import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import org.mockito.ArgumentMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.cdc.usds.simplereport.config.authorization.OrganizationRoleClaims;
import gov.cdc.usds.simplereport.config.authorization.OrganizationRole;
import gov.cdc.usds.simplereport.config.authorization.UserPermission;
import gov.cdc.usds.simplereport.db.model.Organization;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;

class ApiUserManagementTest extends BaseApiTest {

    private static final String NO_USER_ERROR = "Cannot find user.";

    private static final List<String> USERNAMES = List.of("rjj@gmail.com", 
                                                          "rjjones@gmail.com",
                                                          "jaredholler@msn.com",
                                                          "janicek90@yahoo.com");

    @Test
    void whoami_standardUser_okResponses() {
        ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
        assertEquals("Bobbity", who.get("firstName").asText());
        assertEquals("Standard user", who.get("roleDescription").asText());
        assertFalse(who.get("isAdmin").asBoolean());
        assertEquals(OrganizationRole.USER.getGrantedPermissions(), extractPermissionsFromUser(who));
    }

    @Test
    void whoami_entryOnlyUser_okPermissionsAndRoleDescription() {
        useOrgEntryOnly();
        Set<UserPermission> expected = EnumSet.of(UserPermission.START_TEST, UserPermission.SUBMIT_TEST,
                UserPermission.UPDATE_TEST, UserPermission.SEARCH_PATIENTS);
        ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
        assertEquals("Test-entry user", who.get("roleDescription").asText());
        assertFalse(who.get("isAdmin").asBoolean());
        assertEquals(expected, extractPermissionsFromUser(who));
    }

    @Test
    void whoami_orgAdminUser_okPermissionsAndRoleDescription() {
        useOrgAdmin();
        Set<UserPermission> expected = EnumSet.allOf(UserPermission.class);
        ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
        assertEquals("Admin user", who.get("roleDescription").asText());
        assertFalse(who.get("isAdmin").asBoolean());
        assertEquals(expected, extractPermissionsFromUser(who));
    }

    @Test
    void whoami_superuser_okResponses() {
        useSuperUser();
        setRoles(null);
        ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
        assertEquals("Super Admin", who.get("roleDescription").asText());
        assertTrue(who.get("isAdmin").asBoolean());
        assertEquals(Collections.emptySet(), extractPermissionsFromUser(who));
    }

    @Test
    void whoami_nobody_okResponses() {
        setRoles(null);
        ObjectNode who = (ObjectNode) runQuery("current-user-query").get("whoami");
        assertEquals("Misconfigured user", who.get("roleDescription").asText());
        assertFalse(who.get("isAdmin").asBoolean());
        assertEquals(Collections.emptySet(), extractPermissionsFromUser(who));
    }

    @Test
    void addUser_superUser_success() {
        useSuperUser();
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode resp = runQuery("add-user", variables);
        ObjectNode user = (ObjectNode) resp.get("addUser");
        assertEquals("Rhonda", user.get("firstName").asText());
        assertEquals(USERNAMES.get(0), user.get("email").asText());
        assertEquals(_initService.getDefaultOrganization().getExternalId(), 
                user.get("organization").get("externalId").asText());
        assertEquals(OrganizationRole.USER.getGrantedPermissions(), extractPermissionsFromUser(user));
    }

    @Test
    void addUser_adminUser_failure() {
        useOrgAdmin();
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        runQuery("add-user", variables, ACCESS_ERROR);
    }

    @Test
    void addUser_orgUser_failure() {
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        runQuery("add-user", variables, ACCESS_ERROR);
    }

    @Test
    void addUserToCurrentOrg_adminUser_success() {
        useOrgAdmin();
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode resp = runQuery("add-user-to-current-org", variables);
        ObjectNode user = (ObjectNode) resp.get("addUserToCurrentOrg");
        assertEquals("Rhonda", user.get("firstName").asText());
        assertEquals(USERNAMES.get(0), user.get("email").asText());
        assertEquals(_initService.getDefaultOrganization().getExternalId(), 
                user.get("organization").get("externalId").asText());
        assertEquals(OrganizationRole.USER.getGrantedPermissions(), extractPermissionsFromUser(user));
    }

    @Test
    void addUserToCurrentOrg_superUser_failure() {
        useSuperUser();
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        runQuery("add-user-to-current-org", variables, ACCESS_ERROR);
    }

    @Test
    void addUserToCurrentOrg_orgUser_failure() {
        ObjectNode variables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        runQuery("add-user-to-current-org", variables, ACCESS_ERROR);
    }

    @Test
    void updateUser_adminUser_success() {
        useOrgAdmin();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
        String id = addUser.get("id").asText();

        ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III", USERNAMES.get(1));
        ObjectNode updateResp = runQuery("update-user", updateVariables);
        ObjectNode updateUser = (ObjectNode) updateResp.get("updateUser");
        assertEquals("Ronda", updateUser.get("firstName").asText());
        assertEquals(USERNAMES.get(1), updateUser.get("email").asText());
        assertEquals(OrganizationRole.USER.getGrantedPermissions(), extractPermissionsFromUser(updateUser));
    }

    @Test
    void updateUser_superUser_success() {
        useSuperUser();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III", USERNAMES.get(1));
        ObjectNode resp = runQuery("update-user", updateVariables);
        ObjectNode user = (ObjectNode) resp.get("updateUser");
        assertEquals("Ronda", user.get("firstName").asText());
        assertEquals(USERNAMES.get(1), user.get("email").asText());
        assertEquals(OrganizationRole.USER.getGrantedPermissions(), extractPermissionsFromUser(user));
    }

    @Test
    void updateUser_orgUser_failure() {
        useSuperUser();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOrgUser();

        ObjectNode updateVariables = getUpdateUserVariables(id, "Ronda", "J", "Jones", "III", USERNAMES.get(1));
        runQuery("update-user", updateVariables, ACCESS_ERROR);
    }

    @Test
    void updateUser_nonexistentUser_failure() {
        useSuperUser();
        ObjectNode updateVariables = getUpdateUserVariables("fa2efa2e-fa2e-fa2e-fa2e-fa2efa2efa2e", 
                                                            "Ronda", 
                                                            "J", 
                                                            "Jones", 
                                                            "III", 
                                                            USERNAMES.get(1));
        runQuery("update-user", updateVariables, NO_USER_ERROR);
    }

    @Test
    void updateUserRole_adminUser_success() {
        useOrgAdmin();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
        String id = addUser.get("id").asText();

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        
        ObjectNode resp = runQuery("update-user-role", updateRoleVariables);
        assertEquals(OrganizationRole.ADMIN.name(), 
                     resp.get("updateUserRole").asText());
    }

    @Test
    void updateUserRole_superUser_success() {
        useSuperUser();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        
        ObjectNode resp = runQuery("update-user-role", updateRoleVariables);
        assertEquals(OrganizationRole.ADMIN.name(), 
                     resp.get("updateUserRole").asText());
    }

    @Test
    void updateUserRole_outsideOrgAdmin_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOutsideOrgAdmin();

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        
        runQuery("update-user-role", updateRoleVariables, ACCESS_ERROR);
    }
    
    @Test
    void updateUserRole_outsideOrgUser_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOutsideOrgUser();

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        
        runQuery("update-user-role", updateRoleVariables, ACCESS_ERROR);
    }

    @Test
    void updateUserRole_orgUser_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOrgUser();

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        
        runQuery("update-user-role", updateRoleVariables, ACCESS_ERROR);
    }

    @Test
    void setUserIsDeleted_adminUser_success() {
        useOrgAdmin();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user-to-current-org", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUserToCurrentOrg");
        String id = addUser.get("id").asText();

        ObjectNode deleteVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("deleted", true);
        
        ObjectNode resp = runQuery("set-user-is-deleted", deleteVariables);
        assertEquals(USERNAMES.get(0), resp.get("setUserIsDeleted").get("email").asText());

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        runQuery("update-user-role", updateRoleVariables, NO_USER_ERROR);
    }

    @Test
    void setUserIsDeleted_superUser_success() {
        useSuperUser();

        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        ObjectNode deleteVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("deleted", true);
        
        ObjectNode resp = runQuery("set-user-is-deleted", deleteVariables);
        assertEquals(USERNAMES.get(0), resp.get("setUserIsDeleted").get("email").asText());

        ObjectNode updateRoleVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("role", OrganizationRole.ADMIN.name());
        runQuery("update-user-role", updateRoleVariables, NO_USER_ERROR);
    }

    @Test
    void setUserIsDeleted_outsideOrgAdmin_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOutsideOrgAdmin();

        ObjectNode deleteVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("deleted", true);
        runQuery("set-user-is-deleted", deleteVariables, ACCESS_ERROR);
    }
    
    @Test
    void setUserIsDeleted_outsideOrgUser_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOutsideOrgUser();

        ObjectNode deleteVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("deleted", true);
        runQuery("set-user-is-deleted", deleteVariables, ACCESS_ERROR);
    }

    @Test
    void setUserIsDeleted_orgUser_failure() {
        useSuperUser();

        // Adding a user to get a handle on the internal ID
        ObjectNode addVariables = getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId());
        ObjectNode addResp = runQuery("add-user", addVariables);
        ObjectNode addUser = (ObjectNode) addResp.get("addUser");
        String id = addUser.get("id").asText();

        useOrgUser();

        ObjectNode deleteVariables = JsonNodeFactory.instance.objectNode()
                .put("id", id)
                .put("deleted", true);
        runQuery("set-user-is-deleted", deleteVariables, ACCESS_ERROR);
    }

    // The next retrieval test also expects demo users as defined in the no-okta-mgmt profile
    @Test
    void getUsers_adminUser_success() {
        useOrgAdmin();
        
        List<ObjectNode> usersAdded = Arrays.asList(
                getAddUserVariables("Rhonda", "Janet", "Jones", "III", 
                        USERNAMES.get(0), _initService.getDefaultOrganization().getExternalId()),
                getAddUserVariables("Jared", "K", "Holler", null, 
                        USERNAMES.get(2), _initService.getDefaultOrganization().getExternalId()),
                getAddUserVariables("Janice", null, "Katz", "Jr", 
                        USERNAMES.get(3), _initService.getDefaultOrganization().getExternalId()));
        for (ObjectNode userVariables : usersAdded) {
            runQuery("add-user-to-current-org", userVariables);
        }

        ObjectNode resp = runQuery("users-query");
        List<ObjectNode> usersRetrieved = toList((ArrayNode) resp.get("users"));
        
        assertTrue(usersRetrieved.size() > usersAdded.size());

        for (int i = 0; i < usersAdded.size(); i++) {
            ObjectNode userAdded = usersAdded.get(i);
            Optional<ObjectNode> found = usersRetrieved.stream()
                    .filter(u -> u.get("email").asText().equals(userAdded.get("email").asText()))
                    .findFirst();
            assertTrue(found.isPresent());
            ObjectNode userRetrieved = found.get();
            
            assertEquals(userRetrieved.get("firstName").asText(),
                         userAdded.get("firstName").asText());
            assertEquals(userRetrieved.get("email").asText(),
                         userAdded.get("email").asText());
            assertEquals(userRetrieved.get("organization").get("externalId").asText(),
                         userAdded.get("organizationExternalId").asText());
            assertEquals(OrganizationRole.USER.getGrantedPermissions(), 
                         extractPermissionsFromUser(userRetrieved));
        }
    }

    @Test
    void getUsers_orgUser_failure() {
        runQuery("users-query", ACCESS_ERROR);
    }

    private List<ObjectNode> toList(ArrayNode arr) {
        List<ObjectNode> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            list.add((ObjectNode) arr.get(i));
        }
        return list;
    }

    private OrganizationRoleClaims getOrgRoles(OrganizationRole role) {
        Set<OrganizationRole> roles = new HashSet<>();
        roles.add(OrganizationRole.USER);
        roles.add(role);
        return new OrganizationRoleClaims(_initService.getDefaultOrganization().getExternalId(),
                                                   roles);
    }

    private ObjectNode getAddUserVariables(String firstName, 
                                           String middleName, 
                                           String lastName, 
                                           String suffix, 
                                           String email, 
                                           String orgExternalId) {
        ObjectNode variables = JsonNodeFactory.instance.objectNode()
            .put("firstName", firstName)
            .put("middleName", middleName)
            .put("lastName", lastName)
            .put("suffix", suffix)
            .put("email", email)
            .put("organizationExternalId", orgExternalId);
        return variables;
    }

    private ObjectNode getUpdateUserVariables(String id,
                                           String firstName, 
                                           String middleName, 
                                           String lastName, 
                                           String suffix, 
                                           String email) {
        ObjectNode variables = JsonNodeFactory.instance.objectNode()
            .put("id", id)
            .put("firstName", firstName)
            .put("middleName", middleName)
            .put("lastName", lastName)
            .put("suffix", suffix)
            .put("email", email);
        return variables;
    }

    private Set<UserPermission> extractPermissionsFromUser(ObjectNode user) {
        Iterator<JsonNode> permissionsIter = user.get("permissions").elements();
        Set<UserPermission> permissions = new HashSet<>();
        while (permissionsIter.hasNext()) {
            permissions.add(UserPermission.valueOf(permissionsIter.next().asText()));
        }
        return permissions;
    }
}
