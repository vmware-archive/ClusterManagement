package voya.client;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import voya.gemfire.core.cache.manager.RegionOptionsParser;

@ContextConfiguration("/META-INF/spring/gemfire/spring-gemfire-client-cache.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RegionOptionsParserTest {

	@Before
	public void setup() {

	}

	@Test
	public void testReadAllRegionOptions() {

		try {
			RegionOptionsParser.returnAllRegionOptions();
			RegionOptionsParser.returnUserDefinedRegionOptions("config/gemfire/Account.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
