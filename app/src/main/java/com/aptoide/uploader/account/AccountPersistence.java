package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface AccountPersistence {

  Observable<AptoideAccount> getAccount();

  Completable save(AptoideAccount account);

  Completable remove();
}
