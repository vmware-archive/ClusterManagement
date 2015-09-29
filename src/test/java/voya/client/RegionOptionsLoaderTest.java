package voya.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import voya.gemfire.core.cache.manager.OptionsValidator;
import voya.gemfire.core.cache.manager.RegionCreator;
import voya.gemfire.core.cache.manager.RegionOptionsLoader;

@ContextConfiguration("/META-INF/spring/gemfire/spring-gemfire-client-cache.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RegionOptionsLoaderTest {

	@Before
	public void setup() {

	}

	@Test
	public void testReadAllRegionOptions() {

		try {
			OptionsValidator validator = new OptionsValidator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testReadUserDefinedRegionOptions() {

		RegionCreator regionCreator = new RegionCreator();
		regionCreator.init();
		Map<String, String> userDefinedRegionOptions = null;
		userDefinedRegionOptions = regionCreator.loadValidatedRegionOptions("Account");
		
		Assert.isTrue(!userDefinedRegionOptions.isEmpty());
	}

	@Test
	public void testRegionPropertyValidation() {

		RegionOptionsLoader regionOptionsReader = new RegionOptionsLoader();
		Map<String, String> userDefinedRegionOptions = null;
		userDefinedRegionOptions = regionOptionsReader.loadUserDefinedRegionOptions("config/gemfire/Account.properties");
		List<String> errors = new ArrayList<String>();
		boolean areAllOptionsValid = false;
		OptionsValidator validator = null;
		
		try {
			validator = new OptionsValidator();
			areAllOptionsValid = validator.validateRegionProperties(userDefinedRegionOptions, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.isTrue(areAllOptionsValid);
		Assert.isTrue(errors.isEmpty());
	}
	
	@Test
	public void testMispelledOptionName() {
		
		Map<String, String> userDefinedRegionOptions = new HashMap<String, String>();
		userDefinedRegionOptions.put("badOption", "1");

		List<String> errors = new ArrayList<String>();
		boolean areAllOptionsValid = false;
		OptionsValidator validator = null;
		
		try {
			validator = new OptionsValidator();
			areAllOptionsValid = validator.validateRegionProperties(userDefinedRegionOptions, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.isTrue(!areAllOptionsValid);
		Assert.isTrue(errors.size() > 0);
	}

	@Test
	public void testBadOptionType() {
		
		Map<String, String> userDefinedRegionOptions = new HashMap<String, String>();
		userDefinedRegionOptions.put("entry-idle-time-expiration", "1");
	
		List<String> errors = new ArrayList<String>();
		boolean areAllOptionsValid = false;
		OptionsValidator validator = null;
		
		try {
			validator = new OptionsValidator();
			areAllOptionsValid = validator.validateRegionProperties(userDefinedRegionOptions, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.isTrue(areAllOptionsValid);
		Assert.isTrue(errors.size() == 0);
		
		userDefinedRegionOptions.put("entry-idle-time-expiration", "x");
		areAllOptionsValid = validator.validateRegionProperties(userDefinedRegionOptions, errors);

		Assert.isTrue(!areAllOptionsValid);
		Assert.isTrue(errors.size() > 0);

	}
}
