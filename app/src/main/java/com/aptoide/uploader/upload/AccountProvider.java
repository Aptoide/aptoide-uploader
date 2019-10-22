package com.aptoide.uploader.upload;

import com.aptoide.uploader.account.Account;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface AccountProvider {
  Observable<Account> getAccount();

  Single<String> getToken();

  Single<String> revalidateAccessToken();

  Single<String> getRefreshToken();
}
