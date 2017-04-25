package com.geode.management.client.service;

import com.geode.management.domain.Account;

/**
 * The AccountService class is a Service component for processing VOYA Accounts.
 *
 * @author jb
 * @see com.geode.management.domain.Account
 */
public interface AccountService {

  Account getAccount(Long accountId);

}
