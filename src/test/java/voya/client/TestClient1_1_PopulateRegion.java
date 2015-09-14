package voya.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.pdx.PdxInstance;

import voya.client.dao.AccountDao;
import voya.client.service.AccountService;
import voya.core.domain.Account;
import voya.gemfire.core.cache.manager.RegionCreationGemFireCacheManager;
import voya.gemfire.core.cache.manager.RegionCreator;
import voya.gemfire.core.cache.manager.RegionCreator.RegionCreationStrategy;
import voya.gemfire.core.cache.manager.RegionCreator.ServerSideRegionCreationStrategy;

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

  @Autowired
  private RegionCreationStrategy regionCreationStrategy;

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
//	   doTestCacheableHit();
  }

  private void doTestCacheCreationStatus_Positive() {

	  String regionName = "Test";
	  PdxInstance regionOptions = regionCreator.readRegionOptions(regionName);
	  String remoteRegionCreationStatus = regionCreationStrategy.createRegion(regionName,
			  regionOptions, pool);
	  assertEquals(SUCCESSFUL, remoteRegionCreationStatus);

	  remoteRegionCreationStatus = regionCreationStrategy.createRegion(regionName,
			  regionOptions, pool);
	  assertEquals(ALREADY_EXISTS, remoteRegionCreationStatus);

  }

  // JON DOE
  private void doTestCacheableMiss() {
    assertEquals(0, accountDao.getCacheMissCount());

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
}
