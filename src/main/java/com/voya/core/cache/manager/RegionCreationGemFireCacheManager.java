package com.voya.core.cache.manager;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.gemfire.GemfireSystemException;
import org.springframework.data.gemfire.support.GemfireCache;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.voya.core.functions.CreateRegionFunction;

/**
 * The RegionCreationCacheManager class is a GemfireCacheManager extension and implementation of the Spring Cache management
 * abstraction for caching the results of Service method calls.
 *
 * @author jb
 * @see org.springframework.cache.Cache
 * @see org.springframework.data.gemfire.support.GemfireCacheManager
 * @see com.voya.core.functions.VersionAwareRegionGetFunction
 */
public class RegionCreationGemFireCacheManager extends GemfireCacheManager {

  protected final Logger log = Logger.getLogger(getClass().getName());

	@Autowired
	ClientCache bankCache;

	@Autowired
	Pool pool;

  @Autowired
  private RegionCreationStrategy regionCreationStrategy;

  @PostConstruct
  public void init() {
    getRegionCreationStrategy();
    log.info(String.format("Using... (%1$s)", ObjectUtils.nullSafeClassName(getRegionCreationStrategy())));
    log.info(String.format("%1$s initialized!", getClass().getSimpleName()));
  }

	@Override
	public Cache getCache(String cacheName) throws GemfireSystemException{
		Cache cache = super.getCache(cacheName);

		if(cache == null) {
			
			boolean remoteRegionCreated = getRegionCreationStrategy().createRegion(cacheName, pool);
			if(remoteRegionCreated){
				
				Region<?, ?> region = bankCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(cacheName);
				cache = new GemfireCache(region);
				addCache(cache);
			}else {
				throw new GemfireSystemException(new RuntimeException("Could not create remote region"));
			}
		}
		return cache;
	}

  protected RegionCreationStrategy getRegionCreationStrategy() {
    Assert.state(regionCreationStrategy != null,
      "A reference to a RegionCreationStrategy was not properly configured and initialied!");
    return regionCreationStrategy;
  }

  public static interface RegionCreationStrategy {
    boolean createRegion(String regionNameToCreate, Pool pool);
  }

  public static class ServerSideRegionCreationStrategy implements RegionCreationStrategy {

    @Override
    public boolean createRegion(String regionNameToCreate, Pool pool) {
    	Execution fnExec = FunctionService.onServer(pool).withArgs(regionNameToCreate);
    	
    	ResultCollector<?, ?> collector = fnExec.execute(CreateRegionFunction.FUNCTION_ID);
    	List<?> results = (List<?>) collector.getResult();
    	boolean wasRegionCreated = (Boolean) results.get(0);
    	Assert.isTrue(wasRegionCreated);
		return wasRegionCreated;
    }
  }
}
