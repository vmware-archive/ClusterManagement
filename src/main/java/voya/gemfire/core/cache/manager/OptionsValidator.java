package voya.gemfire.core.cache.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the entire set of all possible region options used in region creation via gfsh
 * @author wwilliams Pivotal
 *
 */
public class OptionsValidator {

  private final Logger log = LoggerFactory.getLogger(getClass().getName());

  private BufferedReader csvReader;
  private List<String> errorLog = new ArrayList<String>();
  private static final String fileName = "config/gemfire/all_properties.csv";
  private Map<String, String> allRegionOptions = new HashMap<String, String>();
  
  public OptionsValidator() throws IOException {
      allRegionOptions = loadAllRegionOptions();
  }
  
  /**
   * Converts a CSV file of Key-Value pairs into a Map<String, String>
   * @return a map of unique key-value pairs
   * @throws IOException
   */
  public Map<String, String> loadAllRegionOptions() throws IOException {

    initializeAllOptionsReader(fileName);

    String regionOption;
    try {
      while ((regionOption = csvReader.readLine()) != null) {
        String[] regionOptionKeyValue = regionOption.split(",");
        allRegionOptions.put(regionOptionKeyValue[0], regionOptionKeyValue[1]);
      }
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    finally {
      closeAllOptionsReader();
    }
    
    return allRegionOptions;
  }

  private void initializeAllOptionsReader(String fileName) {
    InputStream inputStream = 
            getClass().getClassLoader().getResourceAsStream(fileName);
  
    csvReader = new BufferedReader(new InputStreamReader(inputStream));
  }

  private void closeAllOptionsReader() {
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
   * @param errors - user container for this method to put validation errors
   * @return
   */
  public boolean validateRegionProperties(Map<String, String> regionProperties, List<String> errors) {
    
    for (String regionOptionName : allRegionOptions.keySet()) {
      if (regionProperties.containsKey(regionOptionName)) {
        validateRegionOptionType(
            regionOptionName, 
            regionProperties.get(regionOptionName), 
            allRegionOptions.get(regionOptionName), 
            errors);
      }
    }
    
    // check for misspelled user defined region options
    for (String regionOptionName : regionProperties.keySet()) {
      if (!allRegionOptions.containsKey(regionOptionName)) {
        errors.add(regionOptionName + ": is not a valid defined region option in " + fileName);
      }
    }
    
    return errors.isEmpty();
  }
  

  void validateRegionOptionType(String optionName, String value, String type, List<String> errors) {

    switch(type) {
    
      case "Integer" : 
        try {
          Integer.parseInt(value);
        }
        catch(Exception ex) {
          errors.add(optionName + ": Could not convert " + value + " to " + type);
        }
        break;
  
      case "Boolean" : 
        try {
          Boolean.parseBoolean(value);
        }
        catch(Exception ex) {
          errors.add(optionName + ": Could not convert " + value + " to " + type);
        }
        break;
        
      case "Long" : 
        try {
          Long.parseLong(value);
        }
        catch(Exception ex) {
          errors.add(optionName + ": Could not convert " + value + " to " + type);
        }
        break;
        
      case "String" : 
        break;
        
      case "String[]" : 
        try {
          value.split(",");
        }
        catch(Exception ex) {
          errors.add(optionName + ": Could not convert " + value + " to " + type + ". Are you missing a comma?");
        }
        break;
        
      default :
        errors.add(optionName + ": Could not convert " + value + " to " + type + ". I do not understand the type " + type);
    }
  }
}
