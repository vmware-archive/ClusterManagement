package com.voya.core.domain;

import com.voya.core.domain.support.Versionable;

import org.springframework.util.ObjectUtils;

/**
 * The Account class is an abstract data type (ADT) for modeling a VOYA Account.
 *
 * @author jb
 * @see com.voya.core.domain.support.Versionable
 */
  public class Account implements Versionable {

  private static int moduleVersion = 1;

  private Long id;

  private String firstName;
  private String lastName;
  private int version = moduleVersion;

  public Account() {
  }

  public Account(final Account account) {
    id = account.getId();
    firstName = account.getFirstName();
    lastName = account.getLastName();
  }

  public static int moduleVersion() {
	  return moduleVersion;
  }
  
  public int getVersion() {
    return version;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Account)) {
      return false;
    }

    Account that = (Account) obj;

    return (this.getVersion() == that.getVersion()
      && equalsIgnoreNull(this.getId(), that.getId())
      && ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName())
      && ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName()));
  }

  protected boolean equalsIgnoreNull(final Object expected, final Object actual) {
    return (expected == null || actual == null || actual.equals(expected));
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getVersion());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getFirstName());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getLastName());
    return hashValue;
  }

  @Override
  public String toString() {
    return String.format("{ @type = %1$s, @version = %2$d, id = %3$d, firstName = %4$s, lastName = %5$s }",
      getClass().getName(), getVersion(), getId(), getFirstName(), getLastName());
  }

}
