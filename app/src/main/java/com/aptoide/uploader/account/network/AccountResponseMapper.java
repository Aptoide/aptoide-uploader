package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.AptoideAccount;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountResponseMapper {

  public AptoideAccount map(AccountResponse response) {
    AccountResponse.Store store = response.getNodes()
        .getMeta()
        .getData()
        .getStore();
    return new AptoideAccount(store != null, true, store != null ? store.getName() : null);
  }
}
