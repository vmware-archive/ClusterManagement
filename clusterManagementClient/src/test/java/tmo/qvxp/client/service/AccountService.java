package tmo.qvxp.client.service;

import tmo.qvxp.core.domain.Account;

/**
 * The AccountService class is a Service component for processing VOYA Accounts.
 *
 * @author jb
 * @see tmo.qvxp.core.domain.Account
 */
public interface AccountService {

  Account getAccount(Long accountId);

}
