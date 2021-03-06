package gov.cdc.usds.simplereport.db.repository;

import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Person;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

/** Interface specification for fetching and manipulating {@link Person} entities */
public interface PersonRepository extends EternalAuditedEntityRepository<Person> {

  public List<Person> findAll(Specification<Person> searchSpec, Pageable p);

  public int count(Specification<Person> searchSpec);

  @Query(
      BASE_ALLOW_DELETED_QUERY
          + " e.isDeleted = :isDeleted AND e.internalId = :id and e.organization = :org")
  public Optional<Person> findByIdAndOrganization(UUID id, Organization org, boolean isDeleted);
}
