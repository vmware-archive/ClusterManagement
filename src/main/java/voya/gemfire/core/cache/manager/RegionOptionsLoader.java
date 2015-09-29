package voya.gemfire.core.cache.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireSystemException;
import org.supercsv.io.ICsvBeanReader;

public class RegionOptionsLoader {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private ICsvBeanReader beanReader;
	private List<String> errorLog = new ArrayList<String>();
	private Map<String, String> userDefinedRegionOptions = new HashMap<String, String>();
	private static final String DEFAULT_REGION_OPTIONS_FILE = "config/gemfire/default.properties";
	
    private Properties props = new Properties();

    /**
     * Loads the user defined properties for the dynamically created region 
     * @param fileName
     * @return
     * @throws IOException
     */
	public Map<String, String> loadUserDefinedRegionOptions(String fileName) {

        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(fileName);
            if (is == null) {
            	loadDefaultRegionOptions();
            }
            else {
            	props.load(is);
            }
        } catch (FileNotFoundException ex) {
        		loadDefaultRegionOptions();
        } catch (IOException ex) {
        	log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException("Error reading Region Options file " + fileName + ".\n" + ex.getMessage()));
        } catch (Exception ex) {
        	log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException("Error processing Region Options file " + fileName + ".\n" + ex.getMessage()));
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
          userDefinedRegionOptions.put(key, props.getProperty(key));
        }

		return userDefinedRegionOptions;
	}
	

    /**
     * Loads the user defined properties for the dynamically created region 
     * @param fileName
     * @return
     * @throws IOException
     */
	public void loadDefaultRegionOptions() {

        InputStream is = null;
        try {
    		is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_REGION_OPTIONS_FILE);
    		props.load(is);
        } catch (FileNotFoundException ex) {
    		log.info("Could not find file " + DEFAULT_REGION_OPTIONS_FILE);
    		throw new GemfireSystemException(new RuntimeException("Could not locate Region Options file " + DEFAULT_REGION_OPTIONS_FILE));
        } catch (IOException ex) {
        	log.info(ex.toString());
			throw new GemfireSystemException(new RuntimeException("Error reading Region Options file " + DEFAULT_REGION_OPTIONS_FILE + ".\n" + ex.getMessage()));
        } finally {
        	if (is != null)
				try {
					is.close();
				} catch (IOException ex) {
		        	log.info(ex.toString());
				}
    	}
	}
        

}
