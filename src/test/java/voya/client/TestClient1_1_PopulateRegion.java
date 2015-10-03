package voya.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import voya.client.dao.AccountDao;
import voya.client.service.AccountService;
import voya.core.domain.Account;
import voya.gemfire.core.cache.manager.RegionCreator;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.internal.cache.tier.sockets.command.CreateRegion;

/**
 * The TestClient class is a test suite of test classes testing data application data versioning using GemFire
 * and Spring's Cache abstraction.
 *
 * @author jb
 * @see org.junit.Test
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 */
@ContextConfiguration("/META-INF/spring/gemfire/spring-gemfire-client-cache.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("unused")
public class TestClient1_1_PopulateRegion {

  private Account jonDoe;
  private Account janeDoe;
  private Account pieDoe;
  private Account cookieDoe;
//  private final Logger log = Logger.getLogger(getClass().getName());
  protected final Logger log = LoggerFactory.getLogger(getClass().getName());

  private static final String SUCCESSFUL = "successful";
  private static final String ALREADY_EXISTS = "alreadyExists";

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private AccountService accountService;

  @Resource(name="serverConnectionPool")
  private Pool pool;

  @Autowired
  private RegionCreator regionCreator;

//  @Resource(name = "Accounts")
//  private Region<Long, Account> accountsRegion;
  private Map<Long, Account> accounts = null;

  @Before
  public void setup() {
  // dummy call to create the regions
  accounts = new HashMap<Long, Account>();
  try {
    Account localJonDoeOne = accountService.getAccount(1L);
  } catch(EmptyResultDataAccessException daoException) {
    log.warn(daoException.getMessage());
  }

  // populate mock application client Account source
    jonDoe = accountDao.save(newAccount("Jon", "Doe"));
    janeDoe = accountDao.save(newAccount("Jane", "Doe"));
    pieDoe = accountDao.save(newAccount("Pie", "Doe"));
    cookieDoe = accountDao.save(newAccount("Cookie", "Doe"));

    accounts.put(pieDoe.getId(), newAccount(pieDoe));
   }

  protected Account newAccount(final Account templateAccount) {
    Account account = newAccount(templateAccount.getFirstName(), templateAccount.getLastName());
    account.setId(templateAccount.getId());
    return account;
  }

  protected Account newAccount(final String firstName, final String lastName) {
    Account account = new Account(firstName, lastName);
    return account;
  }

  @Test
  public void testCacheCreationStatus() {
    doTestCacheCreationStatus_Positive();
  }

  /**
   * Precondition: one account in the cache; PieDoe
   *               JonDoe is not in the cache.
   * PostCondition: two accounts in the cache; JonDoe and PieDoe
   *       JonDoe will be populated as a result of @Cacheable
   */
  @Test
  public void testCacheable() {
     doTestCacheableMiss();
//     doTestCacheableHit();
  }

  private void doTestCacheCreationStatus_Positive() {

    String regionName = "Test";
    Map<String, String> regionOptions = regionCreator.loadValidatedRegionOptions(regionName);
    String remoteRegionCreationStatus = regionCreator.createRegion(regionName,
        regionOptions, pool);
    // both of the following are ok
    if (remoteRegionCreationStatus.equals(SUCCESSFUL)) {
    	assertEquals(SUCCESSFUL, remoteRegionCreationStatus);
    }
    else {
        if (remoteRegionCreationStatus.equals(ALREADY_EXISTS)) {
        	assertEquals(ALREADY_EXISTS, remoteRegionCreationStatus);
        }
    }

    remoteRegionCreationStatus = regionCreator.createRegion(regionName,
      regionOptions, pool);
    assertEquals(ALREADY_EXISTS, remoteRegionCreationStatus);

  }

  @Test
  public void testRegionNameInvalidCharacter() {

    String regionName = "Te\\st";
    Map<String, String> regionOptions = regionCreator.loadValidatedRegionOptions(regionName);
    String remoteRegionCreationStatus = null;
    try {
	    remoteRegionCreationStatus = regionCreator.createRegion(regionName,
	        regionOptions, pool);
	    // both of the following are ok
	    if (remoteRegionCreationStatus.equals(SUCCESSFUL)) {
	    	assertEquals(SUCCESSFUL, remoteRegionCreationStatus);
	    }
	    else {
	        if (remoteRegionCreationStatus.equals(ALREADY_EXISTS)) {
	        	assertEquals(ALREADY_EXISTS, remoteRegionCreationStatus);
	        }
	    }
	
	    remoteRegionCreationStatus = regionCreator.createRegion(regionName,
	      regionOptions, pool);
	    assertEquals(ALREADY_EXISTS, remoteRegionCreationStatus);
    }
    catch (Exception ex) {
    	Assert.isNull(remoteRegionCreationStatus);
    }
  }


    @Test
    public void testRegionCreationWithMisspelledOptionName() {
      Random r = new Random(System.currentTimeMillis());
      String regionName = "Test" + r.nextInt(1000);
      Map<String, String> regionOptions = regionCreator.loadValidatedRegionOptions(regionName);
      regionOptions.put("badOption", "badValue");
      String remoteRegionCreationStatus = null;
  	  remoteRegionCreationStatus = regionCreator.createRegion(regionName,
  	        regionOptions, pool);
  	  // both of the following are ok
  	  if (remoteRegionCreationStatus.equals(SUCCESSFUL) ||
  	    remoteRegionCreationStatus.equals(ALREADY_EXISTS)) {
  	    fail("This should have failed on a bad option");
  	  }
  	  else {
  	    assertTrue(true);
      }
  }

  // JON DOE
  private void doTestCacheableMiss() {
//    assertEquals(0, accountDao.getCacheMissCount());

    // the following will not be in the gemfire cache and so will be counted as a cache miss
    Account localJonDoeOne = accountService.getAccount(jonDoe.getId());

    assertNotNull(localJonDoeOne);
    assertNotSame(jonDoe, localJonDoeOne);
    assertEquals(jonDoe, localJonDoeOne);
    //assertEquals(1, accountDao.getCacheMissCount());

    // this is now in the cache and so will not be regarded as a miss
    Account localJonDoeTwo = accountService.getAccount(jonDoe.getId());

    // assert that the object I got from the cache is equal to what I had originally put into it
    assertEquals(localJonDoeOne, localJonDoeTwo);
  }
  



/*  // PIE DOE
  private void doTestCacheableHit() {
    assertEquals(1, accountDao.getCacheMissCount());

    Account cachePieDoe = accountsRegion.get(pieDoe.getId());

    assertNotNull(cachePieDoe);
    assertNotSame(pieDoe, cachePieDoe);
    assertEquals(pieDoe, cachePieDoe);

    Account localPieDoeOne = accountService.getAccount(pieDoe.getId());

    assertNotNull(localPieDoeOne);
    assertEquals(cachePieDoe, localPieDoeOne);
    assertNotSame(pieDoe, localPieDoeOne);
    assertEquals(pieDoe, localPieDoeOne);
    assertEquals(1, accountDao.getCacheMissCount());

    Account localPieDoeTwo = accountService.getAccount(pieDoe.getId());

    assertEquals(localPieDoeOne, localPieDoeTwo);
    assertEquals(1, accountDao.getCacheMissCount());
  }*/
  
  private ClassPathResource classPathResource(String regionFilename) throws IOException {

      String fileName = "config/gemfire/all_properties.csv";

      ClassPathResource cpr = null;
      cpr = new ClassPathResource(fileName);
      if (!cpr.exists()) {
        throw new IOException("all_properties file must exist for validation to occur.");
      }
    return cpr;
  }

}
