package com.voya.core.cache.manager;

import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import com.voya.core.functions.VersionAwareRegionGetFunction;
import com.gemstone.gemfire.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The VersionedCacheManager class is a GemfireCacheManager extension and implementation of the Spring Cache management
 * abstraction for caching the results of Service method calls.
 *
 * @author jb
 * @see org.springframework.cache.Cache
 * @see org.springframework.data.gemfire.support.GemfireCacheManager
 * @see com.voya.core.functions.VersionAwareRegionGetFunction
 */
public class VersionAwareGemfireCacheManager extends GemfireCacheManager {

  protected final Logger log = Logger.getLogger(getClass().getName());

  @Value("${voya.client.domain.model.version}")
  private int clientVersion;

  @Autowired
  private VersionedDataProcessingStrategy versionedDataProcessingStrategy;

  @PostConstruct
  public void init() {
    getVersionedDataProcessingStrategy();
    log.info(String.format("Using... (%1$s)", ObjectUtils.nullSafeClassName(getVersionedDataProcessingStrategy())));
    log.info(String.format("%1$s initialized!", getClass().getSimpleName()));
  }

  @Override
  protected Cache decorateCache(final Cache cache) {
    return new VersionAwareGemFireCacheDecorator(cache);
  }

  protected VersionedDataProcessingStrategy getVersionedDataProcessingStrategy() {
    Assert.state(versionedDataProcessingStrategy != null,
      "A reference to a VersionedDataProcessingStrategy was not properly configured and initialied!");
    return versionedDataProcessingStrategy;
  }

  protected class VersionAwareGemFireCacheDecorator implements Cache {

    private final Cache cache;

    private VersionAwareGemFireCacheDecorator(final Cache cache) {
      Assert.notNull(cache, "The Cache used for delegating data access to must not be null!");
      this.cache = cache;
    }

    @Override
    public String getName() {
      return cache.getName();
    }

    @Override
    public Object getNativeCache() {
      return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(final Object key) {
      Assert.isInstanceOf(Region.class, getNativeCache(), String.format(
        "The underlying data store must be a GemFire Cache Region; but was (%1$s)!",
          ObjectUtils.nullSafeClassName(getNativeCache())));

      log.info(String.format("%1$s.get(%2$s)", VersionAwareGemfireCacheManager.class.getSimpleName(), key));

      return getVersionedDataProcessingStrategy().get((Region<?, ?>) getNativeCache(), key, clientVersion);
    }

    @Override
    public void put(final Object key, final Object value) {
      cache.put(key, value);
    }

    @Override
    public void evict(final Object key) {
      cache.evict(key);
    }

    @Override
    public void clear() {
      cache.clear();
    }
  }

  public static interface VersionedDataProcessingStrategy {
    ValueWrapper get(Region<?, ?> targetRegion, Object key, Integer clientVersion);
  }

  protected static abstract class AbstractVersionedDataProcessingStrategy implements VersionedDataProcessingStrategy {

    protected ValueWrapper newValueWrapper(final Object value) {
      return (value != null ? new SimpleValueWrapper(value) : null);
    }
  }

  public static class ClientSideVersionedDataProcessingStrategy extends AbstractVersionedDataProcessingStrategy {

    @Override
    public ValueWrapper get(final Region<?, ?> targetRegion, final Object key, final Integer clientVersion) {
      return newValueWrapper(new VersionAwareRegionGetFunction().doGet(targetRegion, key, clientVersion));
    }
  }

  public static class ServerSideVersionedDataProcessingStrategy extends AbstractVersionedDataProcessingStrategy {

    @Override
    public ValueWrapper get(final Region<?, ?> targetRegion, final Object key, final Integer clientVersion) {
      return newValueWrapper(new GemfireOnRegionFunctionTemplate(targetRegion).executeAndExtract(
        VersionAwareRegionGetFunction.FUNCTION_ID, key, clientVersion));
    }
  }

}
