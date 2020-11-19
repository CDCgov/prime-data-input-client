package gov.cdc.usds.simplereport.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.TestOrder;

public interface TestOrderRepository extends CrudRepository<TestOrder, UUID> {

	public static final String BASE_ORG_QUERY = "from #{#entityName} q "
				+ "where q.organization = :org "
				+ "and q.organization.isDeleted = false "
				+ "and q.patient.isDeleted = false ";

	@Query(BASE_ORG_QUERY + "and q.orderStatus = 'PENDING'")
	@EntityGraph(attributePaths = "patient")
	public List<TestOrder> fetchQueueForOrganization(Organization org);

	@Query(BASE_ORG_QUERY + " and q.orderStatus = 'COMPLETED' ")
	@EntityGraph(attributePaths = "patient")
	public List<TestOrder> fetchPastResultsForOrganization(Organization org);
}
