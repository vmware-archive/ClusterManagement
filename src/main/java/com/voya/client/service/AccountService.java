package com.voya.client.service;

import com.voya.core.domain.Account;

/**
 * The AccountService class is a Service component for processing VOYA Accounts.
 *
 * @author jb
 * @see com.voya.core.domain.Account
 */
public interface AccountService {

  Account getAccount(Long accountId);

}
