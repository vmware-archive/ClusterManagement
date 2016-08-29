package tmo.qvxp.client.service;

import tmo.qvxp.domain.Account;

/**
 * The AccountService class is a Service component for processing VOYA Accounts.
 *
 * @author jb
 * @see tmo.qvxp.domain.Account
 */
public interface AccountService {

  Account getAccount(Long accountId);

}
