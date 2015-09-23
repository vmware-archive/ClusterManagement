package voya.gemfire.core.cache.manager;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireSystemException;
import org.springframework.data.gemfire.support.GemfireCache;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.JSONFormatterException;
import com.gemstone.gemfire.pdx.PdxInstance;


@Component
public class RegionCreator {

//	protected final Logger log = Logger.getLogger(getClass().getName());
	protected final Logger log = LoggerFactory.getLogger(getClass().getName());
	final static Charset ENCODING = StandardCharsets.UTF_8;
    private static final String SUCCESSFUL = "successful";
    private static final String ALREADY_EXISTS = "alreadyExists";
    private static final String FUNCTION_ID = "CreateRegion";
    private static final String DEFAULT_REGION_OPTIONS_JSON = "config/gemfire/default.json";


    @Resource(name="serverConnectionPool")
	private Pool pool;

	@Resource(name="gemfireCache")
	private ClientCache voyaCache;

	@Value("${voya.cache.specs.directory}")
	private String cacheSpecsDir;

	private RegionCreationStrategy regionCreationStrategy = new ServerSideRegionCreationStrategy();

    public void setVoyaCache(ClientCache clientCache) {
	      this.voyaCache = clientCache;
	}

    public void setPool(Pool pool) {
	      this.pool = pool;
	}

	void init() {
	    getRegionCreationStrategy();
	    log.info(String.format("Using... (%1$s)", ObjectUtils.nullSafeClassName(getRegionCreationStrategy())));
	}

    Cache createRegion(String regionName) {

    	Region<?, ?> region = null;
    	PdxInstance regionOptions = readRegionOptions(regionName);

    	String remoteRegionCreationStatus = getRegionCreationStrategy().createRegion(regionName, regionOptions, pool);
    	region = retrieveOrCreateRegionBasedOnRemoteRegionCreationStatus
    			(remoteRegionCreationStatus, regionName);

    	if (region == null) {
//    		log.log(Level.SEVERE, "An error occured during region creation for region: " + regionName + "\n" + remoteRegionCreationStatus);
    		log.error("An error occured during region creation for region: " + regionName + "\n" + remoteRegionCreationStatus);
    		throw new GemfireSystemException(new RuntimeException(remoteRegionCreationStatus));
    	}

		return new GemfireCache(region);
    }

    public RegionCreationStrategy getRegionCreationStrategy() {
      Assert.state(regionCreationStrategy != null,
        "A reference to a RegionCreationStrategy was not properly configured and initialied!");
      return regionCreationStrategy;
    }

    public static interface RegionCreationStrategy {
      String createRegion(String regionNameToCreate, PdxInstance regionOptions, Pool pool);
    }

    public static class ServerSideRegionCreationStrategy implements RegionCreationStrategy {

      @Override
      public String createRegion(String regionNameToCreate, PdxInstance regionOptions, Pool pool) {
    	List<Object> args = new ArrayList<Object>(Arrays.asList(regionNameToCreate, regionOptions));

      	Execution fnExec = FunctionService.onServer(pool).withArgs(args);

      	ResultCollector<?, ?> collector = fnExec.execute(FUNCTION_ID);
      	List<?> results = (List<?>) collector.getResult();
      	String wasRegionCreated = (String) results.get(0);
  		return wasRegionCreated;
      }
    }

    public PdxInstance readRegionOptions(String regionName) {

    	PdxInstance regionOptions = null;
    	InputStream regionOptionsIS = null;
    	Scanner scanner = null;

    	ClassPathResource cpr = returnJsonClassPath(regionName);
    	try {
    		regionOptionsIS = cpr.getInputStream();
    		if (regionOptionsIS != null) {
	    		scanner = new Scanner(regionOptionsIS);
	    		String regionOptionsJson = scanner.useDelimiter("\\Z").next();
		    	regionOptions = validateRegionOptionsJson(regionOptionsJson);
    		} else {
    			throw new NoSuchFileException(cpr.getFilename());
    		}
    	}
		catch (JSONFormatterException ex) {
			log.info("JSONFormatterException: "+ ex.getCause().getMessage() + "\n Is GemFire connected?");
    		throw new GemfireSystemException(new RuntimeException("JSONFormatterException: " + ex.getCause().getMessage()));
		}
    	catch(NoSuchFileException ex) {
    		log.info("Could not find file " + cpr.getPath());
    		throw new GemfireSystemException(new RuntimeException("Could not locate Region Options file " + cpr.getPath()));
    	}
        catch(IOException ex) {
        	log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException("Error reading Region Options file " + cpr.getPath()));
        }
    	finally {
    		if (scanner != null)
    			scanner.close();
    	}
        return regionOptions;
    }

    private PdxInstance validateRegionOptionsJson(String regionOptionsJson) throws JSONFormatterException {
    	PdxInstance regionOptions = null;
    	regionOptions = JSONFormatter.fromJSON(regionOptionsJson);
		return regionOptions;
    }

	private Region<?, ?> retrieveOrCreateRegionBasedOnRemoteRegionCreationStatus(
			String remoteRegionCreationStatus, String regionName) {

		Region<?, ?> region = null;

		switch (remoteRegionCreationStatus) {
		case ALREADY_EXISTS:
			region = voyaCache.getRegion(regionName);
			if (region == null) {
				region = createClientRegion(regionName);
			}
			break;
		case SUCCESSFUL:
			region = createClientRegion(regionName);
			break;
		}

		return region;
	}

	private Region<?, ?> createClientRegion(String regionName) {

		Region<?, ?> region = null;

		try {
			region = voyaCache.createClientRegionFactory
				(ClientRegionShortcut.PROXY).create(regionName);
		} catch (RegionExistsException ex) {
			region = voyaCache.getRegion(regionName);
		}
		return region;
	}

	private ClassPathResource returnJsonClassPath(String regionName) {

    	StringBuffer jsonFileName = new StringBuffer("config/gemfire/");
    	jsonFileName.append(regionName);
    	jsonFileName.append(".json");

    	ClassPathResource cpr = null;
    	cpr = new ClassPathResource(jsonFileName.toString());
    	if(!cpr.exists()) {
    		log.info("Using the default region options JSON document.");
    		String defaultFileName = DEFAULT_REGION_OPTIONS_JSON;
    		cpr = new ClassPathResource(defaultFileName);
    	}
		return cpr;
	}

}
