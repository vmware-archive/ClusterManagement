package com.geode.cache.manager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionExistsException;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.gemfire.GemfireSystemException;
import org.springframework.data.gemfire.support.GemfireCache;
import org.springframework.stereotype.Component;

@Component
public class RegionCreator {

	protected final Logger log = LoggerFactory.getLogger(getClass().getName());
	final static Charset ENCODING = StandardCharsets.UTF_8;
	private static final String SUCCESSFUL = "successful";
	private static final String ALREADY_EXISTS = "alreadyExists";
	private static final String FUNCTION_ID = "GfshFunction";
	private PropertiesValidator propertiesValidator;
	private RegionPropertiesLoader propertiesLoader = new RegionPropertiesLoader();
	private final String PROPERTIES_FILE_LOCATION = "config/gemfire/";

	@Resource(name = "serverConnectionPool")
	private Pool pool;

	@Resource(name = "gemfireCache")
	private ClientCache clientCache;

	@Value("${cache.specs.directory}")
	private String cacheSpecsDir;

	public void setClientCache(ClientCache clientCache) {
		this.clientCache = clientCache;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}

	public void init() {
		try {
			propertiesValidator = new PropertiesValidator();
		} catch (IOException ex) {
			log.info(ex.toString());
			throw new GemfireSystemException(
					new RuntimeException("Error reading Region Properties file all_properties.csv"));
		}
	}

	void validateUserDefinedRegionProperties(Map<String, String> regionProperties, String fileName) {

		List<String> errors = new ArrayList<String>();
		boolean arePropertiesValid = propertiesValidator.validateRegionProperties(regionProperties, errors);

		if (arePropertiesValid)
			return;

		String errorString = "Error found in validating properties file " + fileName + "\n";
		for (String error : errors) {
			errorString += error + "\n";
		}
		throw new GemfireSystemException(new RuntimeException(errorString));
	}

	/**
	 * Orchestrates the cache creation from the properties files
	 * 
	 * @param regionName
	 * @return Cache
	 */
	Cache createRegions(String regionName) {

		Map<String, String> regionProperties = loadValidatedRegionProperties(regionName);

		String remoteRegionCreationStatus = createServerRegion(regionName, regionProperties, pool);
		Region<?, ?> region = retrieveOrCreateClientRegionBasedOnServerRegionStatus(remoteRegionCreationStatus,
				regionName);

		if (region == null) {
			log.error("An error occured during region creation for region: " + regionName + "\n"
					+ remoteRegionCreationStatus);
			throw new GemfireSystemException(new RuntimeException(remoteRegionCreationStatus));
		}

		return new GemfireCache(region);
	}

	/**
	 * Load all properties for a requested region name 
	 * @param regionName
	 * @return
	 */
	public Map<String, String> loadValidatedRegionProperties(String regionName) {
		String userDefinedRegionPropertiesFileName = PROPERTIES_FILE_LOCATION + regionName + ".properties";
		Map<String, String> regionProperties = propertiesLoader
				.loadUserDefinedRegionProperties(userDefinedRegionPropertiesFileName);
		validateUserDefinedRegionProperties(regionProperties, userDefinedRegionPropertiesFileName);
		return regionProperties;
	}

	public String createServerRegion(String regionNameToCreate, Map<String, String> regionProperties, Pool pool) {

		StringBuilder createRegionCommand = new StringBuilder("create region --name=" + regionNameToCreate);

		regionProperties.entrySet()
				.forEach(property -> createRegionCommand.append(" --" + property.getKey() + '=' + property.getValue()));

		Execution fnExec = FunctionService.onServer(pool).withArgs(createRegionCommand.toString());

		ResultCollector<?, ?> collector = fnExec.execute(FUNCTION_ID);
		List<?> results = (List<?>) collector.getResult();
		String result = (String) results.get(0);
		String creationStatus = null;
		if (result.contains("created on") && result.contains(regionNameToCreate)) {
			creationStatus = SUCCESSFUL;
		} else if (result.contains("already exists") && result.contains(regionNameToCreate)) {
			creationStatus = ALREADY_EXISTS;
		} else {
			creationStatus = result;
		}
		return creationStatus;
	}

	private Region<?, ?> retrieveOrCreateClientRegionBasedOnServerRegionStatus(String serverRegionCreationStatus,
			String regionName) {

		Region<?, ?> region = null;

		switch (serverRegionCreationStatus) {
		case ALREADY_EXISTS:
			region = clientCache.getRegion(regionName);
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
			region = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(regionName);
		} catch (RegionExistsException ex) {
			region = clientCache.getRegion(regionName);
		}
		return region;
	}
}
