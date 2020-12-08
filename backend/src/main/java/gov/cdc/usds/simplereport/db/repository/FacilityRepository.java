package gov.cdc.usds.simplereport.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;

public interface FacilityRepository extends AuditedEntityRepository<Facility> {

	public Optional<Facility> findByOrganizationAndInternalId(Organization org, UUID id);
	public Optional<Facility> findByOrganizationAndFacilityName(Organization org, String facilityName);

	public List<Facility> findByOrganizationOrderByCreatedAt(Organization org);
}
