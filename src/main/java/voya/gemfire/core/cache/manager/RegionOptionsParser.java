package voya.gemfire.core.cache.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class RegionOptionsParser {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	public static Map<String, String> returnAllRegionOptions() throws IOException {
		String fileName = "config/gemfire/all_properties.csv";
		String delimiter = ",";

		Map<String, String> regionOptions = readRegionOptionsFile(fileName, delimiter);
		for(Entry<String, String> entry : regionOptions.entrySet()) {
			System.out.println("Key: "+ entry.getKey() + " Value: " + entry.getValue());
		}
		return regionOptions;
	}

	public static Map<String, String> returnUserDefinedRegionOptions(String fileName)
			throws IOException {
		String delimiter = "=";
		Map<String, String> usrDefinedRegionOpts = readRegionOptionsFile(fileName, delimiter);;
		return usrDefinedRegionOpts;
	}

	private static Map<String, String> readRegionOptionsFile(String fileName,
			String delimiter) throws IOException {
		ClassPathResource cpr = new ClassPathResource(fileName);
		InputStream regionOptionsIS = null;
    	Scanner scanner = null;
    	Map<String, String> regionOptions = null;

    	try{
	    	if (cpr.exists()) {
	    		regionOptionsIS = cpr.getInputStream();
	    		regionOptions = new LinkedHashMap<String, String>();
	    		if (regionOptionsIS != null) {
	    			scanner = new Scanner(regionOptionsIS);
	    			while (scanner.hasNext()) {
	    				String tokens[] = scanner.nextLine().split(delimiter);
	    				regionOptions.put(tokens[0], tokens[1]);
	    	        }
	    		}
	    	}
    	} finally {
    		scanner.close();
    	}
		return regionOptions;
	}

	private static Map<String, String> readRegionOptionsFile_2() throws IOException {
		String fileName = "config/gemfire/all_properties.csv";
		ClassPathResource cpr = new ClassPathResource(fileName);
		BufferedReader fileReader = null;
		String line = "";
		String DELIMITER = ",";
		Map<String, String> regionOptions = null;

		if (cpr.exists()) {
			regionOptions = new HashMap<String, String>();
			try {
				fileReader = new BufferedReader(new FileReader(cpr.getPath()));
				while ((line = fileReader.readLine()) != null) {
					String[] tokens = line.split(DELIMITER);
					regionOptions.put(tokens[0], tokens[1]);

				}
			} finally {
				fileReader.close();
			}
		}
		return regionOptions;
	}

}
