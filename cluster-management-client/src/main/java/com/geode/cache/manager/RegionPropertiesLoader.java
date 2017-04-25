package com.geode.cache.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.gemfire.GemfireSystemException;

/**
 * Loads region properties from an external source and make them available to the client
 * 
 * @author wwilliams
 *
 */
public class RegionPropertiesLoader {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private Map<String, String> userDefinedRegionProperties = new HashMap<String, String>();
	private static final String DEFAULT_REGION_PROPERTIES_FILE = "config/gemfire/default.properties";

	private Properties props = new Properties();

	/**
	 * Loads the user defined properties for the dynamically created region.
	 * Look first look for properties defined for the region. If it does not exist, apply default properties
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> loadUserDefinedRegionProperties(String fileName) {

		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(fileName);
			if (is == null) {
				loadDefaultRegionProperties();
			} else {
				props.load(is);
			}
		} catch (FileNotFoundException ex) {
			loadDefaultRegionProperties();
		} catch (IOException ex) {
			log.info(ex.toString());
			throw new GemfireSystemException(
					new RuntimeException("Error reading Region Properties file " + fileName + ".\n" + ex.getMessage()));
		} catch (Exception ex) {
			log.info(ex.toString());
			throw new GemfireSystemException(
					new RuntimeException("Error processing Region Properties file " + fileName + ".\n" + ex.getMessage()));
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ex) {
					log.info(ex.toString());
				}
		}

		// load properties into a map
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>) props.propertyNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			log.info(key + " -- " + props.getProperty(key));
			userDefinedRegionProperties.put(key, props.getProperty(key));
		}

		return userDefinedRegionProperties;
	}

	/**
	 * Loads the user defined properties for the dynamically created region
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public void loadDefaultRegionProperties() {

		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_REGION_PROPERTIES_FILE);
			props.load(is);
		} catch (FileNotFoundException ex) {
			log.info("Could not find file " + DEFAULT_REGION_PROPERTIES_FILE);
			throw new GemfireSystemException(
					new RuntimeException("Could not locate Region Properties file " + DEFAULT_REGION_PROPERTIES_FILE));
		} catch (IOException ex) {
			log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException(
					"Error reading Region Properties file " + DEFAULT_REGION_PROPERTIES_FILE + ".\n" + ex.getMessage()));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					log.info(ex.toString());
				}
			}
		}
	}
}
