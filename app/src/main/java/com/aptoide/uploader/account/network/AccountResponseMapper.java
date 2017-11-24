package com.aptoide.uploader.account.network;

import com.aptoide.uploader.account.AptoideAccount;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountResponseMapper {

  public AptoideAccount map(AccountResponse response) {
    return new AptoideAccount(response.getNodes()
        .getMeta()
        .getData()
        .getStore() != null, true);
  }
}
