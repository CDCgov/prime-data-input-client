package gov.cdc.usds.simplereport.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import gov.cdc.usds.simplereport.db.model.EternalAuditedEntity;

/**
 * Base repository interface for soft-deletable entities. Should implement all CRUD operations, but with
 * restrictions against unintentionally retrieving soft-deleted items.
 *
 * @param <T> a soft-delete-only subclass of {@link EternalAuditedEntity}.
 */
@NoRepositoryBean
public interface EternalAuditedEntityRepository<T extends EternalAuditedEntity> extends AuditedEntityRepository<T> {

	public static final String BASE_QUERY = "from #{#entityName} e where e.isDeleted = false ";
	public static final String BASE_ARCHIVED_QUERY = "from #{#entityName} e where e.isDeleted = true ";

	@Override
	@Query(BASE_QUERY)
	public List<T> findAll();

	@Override
	@Modifying// (flushAutomatically = true) // probably not? It's not clear when this would arise actually
	@Query("update #{#entityName} e set e.isDeleted = true where e = :victim")
	public void delete(T victim);

}
