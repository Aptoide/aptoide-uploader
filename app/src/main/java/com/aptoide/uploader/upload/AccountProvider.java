package com.aptoide.uploader.upload;

import com.aptoide.uploader.account.AptoideAccount;
import io.reactivex.Single;

public interface AccountProvider {
  Single<AptoideAccount> getAccount();

  Single<String> getToken();
}
