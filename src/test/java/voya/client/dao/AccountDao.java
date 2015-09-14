package voya.client.dao;

import voya.core.domain.Account;

/**
 * The AccountsDao interface is a Data Access Object (DAO) defining data access (CRUD-based) operations on VOYA Accounts
 * stored in external data source(s).
 *
 * @author jb
 * @see voya.core.domain.Account
 */
public interface AccountDao {

  int getCacheMissCount();

  Account load(Long accountId);

  Account save(Account account);

}
