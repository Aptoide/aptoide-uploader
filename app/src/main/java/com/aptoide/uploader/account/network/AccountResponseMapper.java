package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.Account;
import com.aptoide.uploader.account.AccountFactory;
import com.aptoide.uploader.account.BaseAccount;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountResponseMapper {

  public Account map(AccountResponse response, BaseAccount.LoginType loginType) {
    AccountResponse.Store store = response.getNodes()
        .getMeta()
        .getData()
        .getStore();
    return AccountFactory.of(store != null, true, store != null ? store.getName() : null,
        loginType);
  }
}
