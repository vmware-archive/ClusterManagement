package com.voya.core.cache.manager;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.gemfire.GemfireSystemException;
import org.springframework.data.gemfire.support.GemfireCache;
import org.springframework.stereotype.Component;
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


@Component
public class RegionCreator {

	protected final Logger log = Logger.getLogger(getClass().getName());
	final static Charset ENCODING = StandardCharsets.UTF_8;

	@Autowired
	private Pool pool;

	@Autowired
	@Resource(name="gemfireClientCache")
	private ClientCache voyaCache;
	

	@Autowired
	private RegionCreationStrategy regionCreationStrategy;

	void init() {
	    getRegionCreationStrategy();
	    log.info(String.format("Using... (%1$s)", ObjectUtils.nullSafeClassName(getRegionCreationStrategy())));
	}
	
    Cache createRegion(String regionName) {
    	
    	Region<?, ?> region = null;
    	List<String> regionOptions = readRegionOptions(regionName);

    	boolean remoteRegionCreated = getRegionCreationStrategy().createRegion(regionName, regionOptions, pool);
		if(remoteRegionCreated){
			region = voyaCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(regionName);
		} else {
			throw new GemfireSystemException(new RuntimeException("Could not create remote region"));
		}
		return new GemfireCache(region);    
    }

    public RegionCreationStrategy getRegionCreationStrategy() {
      Assert.state(regionCreationStrategy != null,
        "A reference to a RegionCreationStrategy was not properly configured and initialied!");
      return regionCreationStrategy;
    }

    public static interface RegionCreationStrategy {
      boolean createRegion(String regionNameToCreate, List<String> regionOptions, Pool pool);
    }

    public static class ServerSideRegionCreationStrategy implements RegionCreationStrategy {

      @Override
      public boolean createRegion(String regionNameToCreate, List<String> regionOptions, Pool pool) {
    	List<Object> args = new ArrayList<Object>(Arrays.asList(regionNameToCreate, regionOptions));
    	  
      	Execution fnExec = FunctionService.onServer(pool).withArgs(args);
      	
      	ResultCollector<?, ?> collector = fnExec.execute(CreateRegionFunction.FUNCTION_ID);
      	List<?> results = (List<?>) collector.getResult();
      	boolean wasRegionCreated = (Boolean) results.get(0);
      	Assert.isTrue(wasRegionCreated);
  		return wasRegionCreated;
      }
    }
    
    public List<String> readRegionOptions(String regionName) {

    	List<String> regionOptions = null;
    	Path path = Paths.get(regionName + ".json");
    	
    	try {
	    	regionOptions = Files.readAllLines(path, ENCODING);
    	}
        catch(FileNotFoundException ex) {
			throw new GemfireSystemException(new RuntimeException("Could not locate Region Options file " + path.toString()));
        }
    	catch(NoSuchFileException ex) {
    		log.info("Could not find file " + path.toAbsolutePath());
    		throw new GemfireSystemException(new RuntimeException("Could not locate Region Options file " + path.toString()));
    	}
        catch(IOException ex) {
        	log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException("Error reading Region Options file " + path.toString()));
        }
        
        return regionOptions;
    }
}
