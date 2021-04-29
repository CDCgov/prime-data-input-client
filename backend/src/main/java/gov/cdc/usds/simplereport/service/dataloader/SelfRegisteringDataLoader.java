package gov.cdc.usds.simplereport.service.dataloader;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

/**
 * In order for the GraphQL DataResolvers to be able to access DataLoaders from the
 * DataLoaderRegistry, each DataLoader must have a unique key. In order for our DataLoaders to
 * <em>self-register</em> with the DataLoaderRegistry, they must also know their own key at
 * instantiation.
 */
public abstract class SelfRegisteringDataLoader<K, V> extends DataLoader<K, V> {
  SelfRegisteringDataLoader(
      DataLoaderRegistry dataLoaderRegistry, BatchLoader<K, V> batchLoadFunction) {
    super(batchLoadFunction);
    dataLoaderRegistry.register(getKey(), this);
  }

  abstract String getKey();
}
