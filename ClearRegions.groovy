import com.gemstone.gemfire.cache.Region
import com.gemstone.gemfire.cache.Cache
import com.gemstone.gemfire.cache.CacheFactory

/*
 * Clears all entries from a region
 * 1) Enter a comma-delimited set of regions that you want to clear
 * 2) Enter the host[port] to one of your server locators
 * 
 * Run as:  groovy -cp $GEMFIRE/lib/server-dependencies.jar ClearRegions
 */
String[] regionNames = ["xxx"]
String locators = "localhost[10334]"

// Connect to the locator
Cache cache = new CacheFactory()
	.set("name", "UtilityClient")
	.set("log-level", "error")
	.set("mcast-port", "0")
	.set("locators", locators)
	.create();

	// delete all entries in the region names
for (String regionName : regionNames) {

	// Get the region
	System.out.println("-----------------------------")
	System.out.println("Processing region " + regionName)
	Region<?, ?> region = cache.getRegion(regionName);
	
	println("Located the region " + region.getName());
	
	// get all the keys in the region
	Set<?> keys = region.keySet()
	System.out.println("removing " + keys.size() + " values");
	
	// remove all values
	region.removeAll(keys)
	println("Remaining entry count: " + region.keySet().size());
	println()
}

System.out.println("Done");
System.out.println();

// Close cache
cache.close();

