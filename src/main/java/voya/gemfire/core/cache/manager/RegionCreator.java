package voya.gemfire.core.cache.manager;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

//  protected final Logger log = Logger.getLogger(getClass().getName());
  protected final Logger log = LoggerFactory.getLogger(getClass().getName());
  final static Charset ENCODING = StandardCharsets.UTF_8;
    private static final String SUCCESSFUL = "successful";
    private static final String ALREADY_EXISTS = "alreadyExists";
    private static final String FUNCTION_ID = "CreateRegion";
    private Map<String, String> regionOptions = null;
    private OptionsValidator optionsValidator;
    private RegionOptionsLoader optionsLoader = new RegionOptionsLoader();

    @Resource(name="serverConnectionPool")
  private Pool pool;

  @Resource(name="gemfireCache")
  private ClientCache voyaCache;

  @Value("${voya.cache.specs.directory}")
  private String cacheSpecsDir;

    public void setVoyaCache(ClientCache clientCache) {
        this.voyaCache = clientCache;
  }

    public void setPool(Pool pool) {
        this.pool = pool;
  }

  public void init() {
    try {
      optionsValidator = new OptionsValidator();
    } catch (IOException ex) {
      log.info(ex.toString());
      throw new GemfireSystemException(new RuntimeException("Error reading Region Options file all_properties.csv"));
    }
  }

  void validateUserDefinedRegionOptions(Map<String, String> regionProperties, String fileName) {

    List<String> errors = new ArrayList<String>();
    boolean areOptionsValid = optionsValidator.validateRegionProperties(regionProperties, errors);

    if (areOptionsValid)
      return;
    
    String errorString = "Error found in validating options file " + fileName + "\n";
    for (String error : errors) {
      errorString += error + "\n";
    }
    throw new GemfireSystemException(new RuntimeException(errorString));
  }

  /**
   * Orchestrates the cache creation from the properties files
   * @param regionName
   * @return Cache
   */
  Cache createRegion(String regionName) {

    Map<String, String> regionOptions = loadValidatedRegionOptions(regionName);
    
    String remoteRegionCreationStatus = createRegion(regionName, regionOptions, pool);
    Region<?, ?> region = retrieveOrCreateRegionBasedOnRemoteRegionCreationStatus
     (remoteRegionCreationStatus, regionName);

    if (region == null) {
    log.error("An error occured during region creation for region: " + regionName + "\n" + remoteRegionCreationStatus);
    throw new GemfireSystemException(new RuntimeException(remoteRegionCreationStatus));
    }

    return new GemfireCache(region);
    }
  
  public Map<String, String> loadValidatedRegionOptions(String regionName) {
      String userDefinedRegionPropertiesFileName = "config/gemfire/" + regionName + ".properties";
      Map<String, String> regionProperties = optionsLoader.loadUserDefinedRegionOptions(userDefinedRegionPropertiesFileName);
      validateUserDefinedRegionOptions(regionProperties, userDefinedRegionPropertiesFileName);
      return regionProperties;
  }

    public String createRegion(String regionNameToCreate, Map<String, String> regionOptions, Pool pool) {
      List<Object> args = new ArrayList<Object>(Arrays.asList(regionNameToCreate, regionOptions));

      Execution fnExec = FunctionService.onServer(pool).withArgs(args);

      ResultCollector<?, ?> collector = fnExec.execute(FUNCTION_ID);
      List<?> results = (List<?>) collector.getResult();
      String wasRegionCreated = (String) results.get(0);
      return wasRegionCreated;
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
}
