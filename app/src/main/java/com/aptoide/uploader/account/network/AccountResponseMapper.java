package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.Account;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountResponseMapper {

  public Account map(AccountResponse response) {
    AccountResponse.Store store = response.getNodes()
        .getMeta()
        .getData()
        .getStore();
    return new Account(store != null, true, store != null ? store.getName() : null);
  }
}
