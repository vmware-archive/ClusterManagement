package com.voya.client;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import com.voya.client.dao.AccountDao;
import com.voya.client.service.AccountService;
import com.voya.core.domain.Account;
import com.gemstone.gemfire.cache.Region;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private AccountService accountService;

  @Resource(name = "Accounts")
  private Region<Long, Account> accountsRegion;

  @Before
  public void setup() {
	  // dummy call to create the regions
	Account localJonDoeOne = accountService.getAccount(1L);
	accountsRegion.clear();
	
	// populate mock application client Account source 
    jonDoe = accountDao.save(newAccount("Jon", "Doe")); 
    janeDoe = accountDao.save(newAccount("Jane", "Doe"));
    pieDoe = accountDao.save(newAccount("Pie", "Doe")); 
    cookieDoe = accountDao.save(newAccount("Cookie", "Doe")); 

    // put one v1 accounts into the cache
    accountsRegion.put(pieDoe.getId(), newAccount(pieDoe));
   }

  @After
  public void tearDown() {
//    accountsRegion.clear();
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

  // JON DOE
  private void doTestCacheableMiss() {
    assertEquals(0, accountDao.getCacheMissCount());

    // the following will not be in the gemfire cache and so will be counted as a cache miss
    Account localJonDoeOne = accountService.getAccount(jonDoe.getId());

    assertNotNull(localJonDoeOne);
    assertNotSame(jonDoe, localJonDoeOne);
    assertEquals(jonDoe, localJonDoeOne);
    assertEquals(1, accountDao.getCacheMissCount());

    // this is now in the cache and so will not be regarded as a miss
    Account localJonDoeTwo = accountService.getAccount(jonDoe.getId());

    // assert that the object I got from the cache is equal to what I had originally put into it
    assertEquals(localJonDoeOne, localJonDoeTwo);
    
    // I still should have only one cache miss
    assertEquals(1, accountDao.getCacheMissCount());
  }
  
  // PIE DOE
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
  }
}
