package com.voya.core.functions;

import java.util.logging.Logger;

import com.voya.core.domain.support.Versionable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The VersionedDataRegionFunction class is a GemFire Function implemented with SDG's Function annotation abstraction,
 * returning Account data based on whether the version is up-to-date in the GemFire Cache Region, "Accounts".
 *
 * @author jb
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @see com.voya.core.domain.Account
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.pdx.PdxInstance
 */
@SuppressWarnings("unused")
public class VersionAwareRegionGetFunction {

  public static final String FUNCTION_ID = "getVersionedCacheValue";

  protected static final String VERSION_PROPERTY_NAME = "version";

  protected final Logger log = Logger.getLogger(getClass().getName());

  @GemfireFunction(id = FUNCTION_ID, hasResult = true)
  public Object get(final FunctionContext functionContext, final Object key, final Integer clientVersion) {
    Assert.isInstanceOf(RegionFunctionContext.class, functionContext, String.format(
      "Function (%1$s) can only be invoked on a Region!", getClass().getSimpleName()));

    return doGet(((RegionFunctionContext) functionContext).getDataSet(), key, clientVersion);
  }

  public Object doGet(final Region<?, ?> targetRegion, final Object key, final Integer clientVersion) {
    Assert.notNull(key, String.format(
      "The key to the versioned data returned by the '%1$s' Function must be specified!", FUNCTION_ID));

    Assert.notNull(targetRegion, String.format(
      "The Region from which to fetch the versioned value for key (%1$s) must not be null!", key));

    Assert.notNull(clientVersion, String.format(
      "The client's version of the versioned Region (%1$s) value must be provided!", targetRegion.getName()));

    log.info(String.format("Fetching value from Region (%1$s) for Key (%2$s)...", targetRegion.getName(), key));

    Object cachedValue = targetRegion.get(key);

    log.info(String.format("The class type of the value is (%2$s).", key,
      ObjectUtils.nullSafeClassName(cachedValue)));

    if (cachedValue instanceof PdxInstance) {
      log.info("Processing PdxInstance...");
      if (((PdxInstance) cachedValue).hasField(VERSION_PROPERTY_NAME)) {
        Integer cachedVersion = (Integer) ((PdxInstance) cachedValue).getField(VERSION_PROPERTY_NAME);
        return (clientVersion > cachedVersion ? null : cachedValue);
      }
      else {
        return cachedValue;
      }
    }
    else if (cachedValue instanceof Versionable) {
      log.info("Processing Versionable POJO...");
      // if cached Account version is greater (newer) than client's Account version, return it, otherwise the client's
      // Account version is newer.
      return (((Versionable) cachedValue).getVersion() > clientVersion ? cachedValue : null);
    }

    log.info("Processing Object...");

    return cachedValue;
  }

}
