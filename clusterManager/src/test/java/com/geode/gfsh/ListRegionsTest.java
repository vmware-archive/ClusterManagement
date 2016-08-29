package com.geode.gfsh;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheXmlException;
import com.geode.gfsh.list.ListCommandsParser;

public class ListRegionsTest {

  private static ListCommands listCommands;
  ListCommandsParser listCommandsStripper = new ListCommandsParser();

    @BeforeClass
    public static void init() {
    	
      System.out.println("##############################");
      System.out.println(Paths.get(".").toAbsolutePath().toString());
      String pathToConfigFile = "./grid/locator1/cluster_config/cluster/cluster.xml";
      
      CacheFactory cf;
      Cache cache;
      try {
      cf = new CacheFactory();
          cf.set("cache-xml-file", pathToConfigFile);
          cf.set("locators", "localhost[10334]");
          cache = cf.create();
      }
      catch (CacheXmlException e) {
        pathToConfigFile = "./cluster.xml";
        cf = new CacheFactory();
          cf.set("cache-xml-file", pathToConfigFile);
          cf.set("locators", "localhost[10334]");
          cache = cf.create();
      }
          
      listCommands = new ListCommands(cache.getLogger());
    }
	@Test
    public void testListRegions() 
    		throws IllegalAccessException, 
    			InvocationTargetException, 
    			RuntimeException {
      String command = "list regions";
      StringTokenizer tokens = new StringTokenizer(command);
      String highLevelCommand = tokens.nextToken();
      String commandResult = listCommands.execute(command);
	  List<String> regionNames = listCommands.parseResults(commandResult, tokens.nextToken(), null);

      Assert.notEmpty(regionNames);
    }

	@Test
    public void testListRegionsLike() 
    		throws IllegalAccessException, 
    			InvocationTargetException, 
    			RuntimeException {
	  String gfshCommand = "list regions";
	  List<String> regionNames = filteredRegionNames(gfshCommand, "Test([0-9]|[1-9][0-9]|[1-9][0-9][0-9])$");

      Assert.notEmpty(regionNames);
    }

	@Test
    public void testListRegionsParser() 
    		throws IllegalAccessException, 
    			InvocationTargetException, 
    			RuntimeException {
	  String command = "list regions";
      List<String> regionNames = filteredRegionNames(command, "Test([0-9]|[1-9][0-9]|[1-9][0-9][0-9])$");

      Assert.notEmpty(regionNames);
    }
	
	private List<String> filteredRegionNames(String command, String regexExpression)
			throws IllegalAccessException,
				IllegalArgumentException, 
				InvocationTargetException {
	      String commandResult = listCommands.execute(command);
	      return listCommandsStripper.listRegionsLike(commandResult, regexExpression);
	}
}
