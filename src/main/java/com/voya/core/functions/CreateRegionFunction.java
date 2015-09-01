package com.voya.core.functions;

	import java.util.List;
import java.util.logging.Logger;

import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.util.Assert;

import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.pdx.PdxInstance;

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
	public class CreateRegionFunction {

	  public static final String FUNCTION_ID = "CreateRegion";
	  private static final String metaDataRegionName = "__metadataRegion";

	  protected final Logger log = Logger.getLogger(getClass().getName());

	  @GemfireFunction(id = FUNCTION_ID, hasResult = true)
	  public Object createRegion(final FunctionContext functionContext, List<Object> args ) {

		String regionNameToCreate = (String) args.get(0);
		PdxInstance regionOptions = (PdxInstance) args.get(1);
		  
	    return doCreate(regionNameToCreate, regionOptions);
	  }

	  public boolean doCreate(String regionNameToCreate, PdxInstance regionOptions) {
		  boolean wasRegionCreated = false;
		  
	    Assert.notNull(regionNameToCreate, String.format(
	      "The Region from which to fetch the versioned value for key (%1$s) must not be null!", regionNameToCreate));

	    //Object cachedValue = metaDataRegionName.put(regionNameToCreate);

	    wasRegionCreated = true;
	    return wasRegionCreated;
	  }
	}
