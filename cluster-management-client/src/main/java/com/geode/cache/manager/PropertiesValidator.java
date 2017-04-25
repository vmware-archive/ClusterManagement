package com.geode.cache.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the entire set of all possible region properties used in region
 * creation via gfsh
 * 
 * @author Pivotal
 *
 */
public class PropertiesValidator {

	// private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private BufferedReader csvReader;
	private List<String> errorLog = new ArrayList<String>();
	private static final String fileName = "config/gemfire/all_properties.csv";
	private Map<String, String> validRegionProperties = new HashMap<String, String>();

	public PropertiesValidator() throws IOException {
		validRegionProperties = loadAllRegionProperties();
	}

	/**
	 * Converts a CSV file of Key-Value pairs into a Map<String, String>
	 * 
	 * @return a map of unique key-value pairs
	 * @throws IOException
	 */
	public Map<String, String> loadAllRegionProperties() throws IOException {

		initializeAllPropertiesReader(fileName);

		String regionProperty;
		try {
			while ((regionProperty = csvReader.readLine()) != null) {
				String[] regionPropertyKeyValue = regionProperty.split(",");
				validRegionProperties.put(regionPropertyKeyValue[0], regionPropertyKeyValue[1]);
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {
			closeAllPropertiesReader();
		}

		return validRegionProperties;
	}

	private void initializeAllPropertiesReader(String fileName) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

		csvReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	private void closeAllPropertiesReader() {
		if (csvReader != null) {
			try {
				csvReader.close();
			} catch (IOException e) {
				errorLog.add(e.toString());
			}
		}
	}

	/**
	 * Returns true if all properties are in the correct format
	 * 
	 * @param regionProperties
	 * @param errors
	 *            - user container for this method to put validation errors
	 * @return
	 */
	public boolean validateRegionProperties(Map<String, String> regionProperties, List<String> errors) {

		validRegionProperties.keySet().forEach(regionPropertyName -> {
			if (regionProperties.containsKey(regionPropertyName)) {
				validateRegionPropertyType(regionPropertyName, regionProperties.get(regionPropertyName),
						validRegionProperties.get(regionPropertyName), errors);
			}
		});

		// check for misspelled user defined region properties
		regionProperties.keySet().forEach(regionPropertyName -> {
			if (!validRegionProperties.containsKey(regionPropertyName)) {
				errors.add(regionPropertyName + ": is not a valid defined region property in " + fileName);
			}
		});

		return errors.isEmpty();
	}

	void validateRegionPropertyType(String propertyName, String value, String type, List<String> errors) {

		switch (type) {

		case "Integer":
			try {
				Integer.parseInt(value);
			} catch (Exception ex) {
				errors.add(propertyName + ": Could not convert " + value + " to " + type);
			}
			break;

		case "Boolean":
			try {
				Boolean.parseBoolean(value);
			} catch (Exception ex) {
				errors.add(propertyName + ": Could not convert " + value + " to " + type);
			}
			break;

		case "Long":
			try {
				Long.parseLong(value);
			} catch (Exception ex) {
				errors.add(propertyName + ": Could not convert " + value + " to " + type);
			}
			break;

		case "String":
			break;

		case "String[]":
			try {
				value.split(",");
			} catch (Exception ex) {
				errors.add(
						propertyName + ": Could not convert " + value + " to " + type + ". Are you missing a comma?");
			}
			break;

		default:
			errors.add(propertyName + ": Could not convert " + value + " to " + type + ". I do not understand the type "
					+ type);
		}
	}
}
