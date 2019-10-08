package com.aptoide.uploader.account;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface AccountPersistence {

  Observable<Account> getAccount();

  Completable save(Account account);

  Completable remove();
}
