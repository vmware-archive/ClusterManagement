package com.voya.client.service.provider;

import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import com.voya.client.dao.AccountDao;
import com.voya.client.service.AccountService;
import com.voya.core.domain.Account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * The CachingAccountsService class is an implementation of the AccountService interface implementing business functions
 * and rules for processing VOYA Accounts.  This implementation supports caching Account information as well as the
 * capability to fetch Account details from an external data source(s).
 *
 * @author jb
 * @see org.springframework.stereotype.Service
 * @see com.voya.core.domain.Account
 * @see com.voya.client.dao.AccountDao
 * @see com.voya.client.service.AccountService
 */
@Service("accountService")
public class CachingAccountService implements AccountService {

  protected final Logger log = Logger.getLogger(getClass().getName());

  @Autowired
  private AccountDao accountDao;

  @PostConstruct
  public void init() {
    getAccountDao();
    log.info(String.format("%1$s initialized!", getClass().getSimpleName()));
  }

  protected AccountDao getAccountDao() {
    Assert.state(accountDao != null, "A reference to the AccountDao was not properly configured and initialized!");
    return accountDao;
  }

  @Cacheable(value = "Customer")
  public Account getAccount(final Long accountId) {
    log.info(String.format("%1$s.get(%2$s): getting Account with ID (%2$s) from external data source!",
      getClass().getSimpleName(), accountId));
    return getAccountDao().load(accountId);
  }

}
