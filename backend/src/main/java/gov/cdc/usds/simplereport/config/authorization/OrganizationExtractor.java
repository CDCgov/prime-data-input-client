package gov.cdc.usds.simplereport.config.authorization;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import gov.cdc.usds.simplereport.config.AuthorizationProperties;

@Component
public class OrganizationExtractor
        implements Converter<Collection<? extends GrantedAuthority>, List<OrganizationRoleClaims>> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationExtractor.class);

    private AuthorizationProperties properties;

    public OrganizationExtractor(AuthorizationProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<OrganizationRoleClaims> convert(Collection<? extends GrantedAuthority> source) {
        Map<String, EnumSet<OrganizationRole>> rolesFound = new HashMap<>();
        for (GrantedAuthority granted : source) {
            String claimed = granted.getAuthority();
            if (!claimed.startsWith(properties.getRolePrefix())) {
                continue;
            }
            int roleOffset = claimed.lastIndexOf(":");
            String claimedOrg = claimed.substring(properties.getRolePrefix().length(), roleOffset);
            String claimedRole = claimed.substring(roleOffset + 1); // the separator is part of neither string
            try {
                OrganizationRole claimedRoleValidated = OrganizationRole.valueOf(claimedRole);
                EnumSet<OrganizationRole> existingRoles = rolesFound.get(claimedOrg);
                if (existingRoles == null) {
                    rolesFound.put(claimedOrg, EnumSet.of(claimedRoleValidated));
                } else {
                    existingRoles.add(claimedRoleValidated);
                }
            } catch (IllegalArgumentException e) {
                LOG.debug("Unexpected role constant {}", claimedRole);
            }
        }
        if (rolesFound.isEmpty()) {
            LOG.error("No tenant organization roles found!");
        }
        return rolesFound.entrySet().stream()
                .map(e -> new OrganizationRoleClaims(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

}
