package com.voya.client;


import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;

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
public class TestClient1_3_UpdateVersions {

	  private Account jonDoe;
	  private Account janeDoe;
	  private Account pieDoe;
	  private Account cookieDoe;
	  
	  private long pieDoeId = 3;
	  private long cookieDoeId = 4;

	  @Autowired
	  private AccountDao accountDao;

	  @Autowired
	  private AccountService accountService;

	  @Resource(name = "Accounts")
	  private Region<Long, Account> accountsRegion;


	  @After
	  public void tearDown() {
//	    accountsRegion.clear();
	  }
	  protected Account newAccount(final Account templateAccount) {
	    Account account = newAccount(templateAccount.getFirstName(), templateAccount.getLastName());
	    account.setId(templateAccount.getId());
	    return account;
	  }

	  protected Account newAccount(final String firstName, final String lastName) {
	    Account account = new Account();
	    account.setFirstName(firstName);
	    account.setLastName(lastName);
	    return account;
	  }

	  @Test
	  public void testVersioning() {
		  doTestUpdateVersion1();
		  doTestUpdateVersion2();
	  }

	  /**
	   * Precondition: V1 account in the cache; JonDoe and PieDoe
	   * PostCondition: PieDoe first name updated to Pi.
	   */
	  // PIE DOE
	  private void doTestUpdateVersion1() {
		int cacheMissCount = accountDao.getCacheMissCount();
		
	    pieDoe = accountsRegion.get(pieDoeId);
	    
	    // assert that we hit the cache and did not get a new object
	    assertEquals(cacheMissCount, accountDao.getCacheMissCount());

	    assertNotNull(pieDoe);
	    pieDoe.setFirstName("Pi");
	    accountsRegion.put(pieDoe.getId(), pieDoe);
	   
	    Account updatedPieDoe = accountService.getAccount(pieDoe.getId());

	    assertEquals(pieDoe, updatedPieDoe);
	    assertEquals(cacheMissCount, accountDao.getCacheMissCount());

	  }
	  
	  /**
	   * Precondition: two V2 accounts in the cache; JaneDoe and CookieDoe
	   * PostCondition: CookieDoe first name updated to Cook.
	   */
	  private void doTestUpdateVersion2() {
			int cacheMissCount = accountDao.getCacheMissCount();

		    cookieDoe = accountService.getAccount(cookieDoeId);
		    
		    // assert that we hit the cache and did not get a new object
		    assertEquals(cacheMissCount, accountDao.getCacheMissCount());

		    assertNotNull(cookieDoe);
		    assertEquals(cookieDoe.getVersion(), 2);
		    cookieDoe.setFirstName("Cook");
		    accountsRegion.put(cookieDoe.getId(), cookieDoe);
		   
		    Account updatedCookieDoe = accountService.getAccount(cookieDoe.getId());

		    assertEquals(cookieDoe, updatedCookieDoe);
		    assertEquals(cacheMissCount, accountDao.getCacheMissCount());
	  }
}
