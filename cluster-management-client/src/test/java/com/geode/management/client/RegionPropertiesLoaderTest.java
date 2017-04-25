package com.geode.management.client;

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

import com.geode.cache.manager.PropertiesValidator;
import com.geode.cache.manager.RegionCreator;
import com.geode.cache.manager.RegionPropertiesLoader;

@ContextConfiguration("/META-INF/spring/gemfire/spring-gemfire-client-cache.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RegionPropertiesLoaderTest {

	@Before
	public void setup() {
	}

	@Test
	public void testReadAllRegionProperties() {
		try {
			new PropertiesValidator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testReadUserDefinedRegionProperties() {

		RegionCreator regionCreator = new RegionCreator();
		regionCreator.init();
		Map<String, String> userDefinedRegionProperties = null;
		userDefinedRegionProperties = regionCreator.loadValidatedRegionProperties("Account");

		Assert.isTrue(!userDefinedRegionProperties.isEmpty());
	}

	/**
	 * Create a region that does not explicitly have region properties defined with the name
	 */
	@Test
	public void testReadDefaultRegionProperties() {

		RegionCreator regionCreator = new RegionCreator();
		regionCreator.init();
		Map<String, String> userDefinedRegionProperties = null;
		userDefinedRegionProperties = regionCreator.loadValidatedRegionProperties("XXX");

		Assert.isTrue(!userDefinedRegionProperties.isEmpty());
	}

	@Test
	public void testRegionPropertyValidation() {

		RegionPropertiesLoader regionPropertiesReader = new RegionPropertiesLoader();
		Map<String, String> userDefinedRegionProperties = null;
		userDefinedRegionProperties = regionPropertiesReader
				.loadUserDefinedRegionProperties("config/gemfire/Account.properties");
		List<String> errors = new ArrayList<String>();
		boolean areAllPropertiesValid = false;
		PropertiesValidator validator = null;

		try {
			validator = new PropertiesValidator();
			areAllPropertiesValid = validator.validateRegionProperties(userDefinedRegionProperties, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.isTrue(areAllPropertiesValid);
		Assert.isTrue(errors.isEmpty());
	}

	@Test
	public void testMispelledPropertyName() {

		Map<String, String> userDefinedRegionProperties = new HashMap<String, String>();
		userDefinedRegionProperties.put("badProperty", "1");

		List<String> errors = new ArrayList<String>();
		boolean areAllPropertiesValid = false;
		PropertiesValidator validator = null;

		try {
			validator = new PropertiesValidator();
			areAllPropertiesValid = validator.validateRegionProperties(userDefinedRegionProperties, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.isTrue(!areAllPropertiesValid);
		Assert.isTrue(errors.size() > 0);
	}

	@Test
	public void testBadPropertyType() {

		Map<String, String> userDefinedRegionProperties = new HashMap<String, String>();
		userDefinedRegionProperties.put("entry-idle-time-expiration", "1");

		List<String> errors = new ArrayList<String>();
		boolean areAllPropertiesValid = false;
		PropertiesValidator validator = null;

		try {
			validator = new PropertiesValidator();
			areAllPropertiesValid = validator.validateRegionProperties(userDefinedRegionProperties, errors);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.isTrue(areAllPropertiesValid);
		Assert.isTrue(errors.size() == 0);

		userDefinedRegionProperties.put("entry-idle-time-expiration", "x");
		areAllPropertiesValid = validator.validateRegionProperties(userDefinedRegionProperties, errors);

		Assert.isTrue(!areAllPropertiesValid);
		Assert.isTrue(errors.size() > 0);
	}
}
