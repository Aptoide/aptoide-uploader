package com.aptoide.uploader.upload;

import com.aptoide.uploader.account.AptoideAccount;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface AccountProvider {
  Observable<AptoideAccount> getAccount();

  Single<String> getToken();
}
