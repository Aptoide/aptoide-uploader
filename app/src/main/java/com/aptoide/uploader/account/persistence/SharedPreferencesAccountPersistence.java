package com.aptoide.uploader.account.persistence;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import com.aptoide.uploader.account.AccountPersistence;
import com.aptoide.uploader.account.AptoideAccount;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;

public class SharedPreferencesAccountPersistence implements AccountPersistence {

  private static final String HAS_STORE = "HAS_STORE";
  private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
  private static final String STORE_NAME = "store_name";
  private final PublishSubject<AptoideAccount> accountSubject;
  private final SharedPreferences preferences;
  private final Scheduler scheduler;

  public SharedPreferencesAccountPersistence(PublishSubject<AptoideAccount> accountSubject,
      SharedPreferences preferences, Scheduler scheduler) {
    this.accountSubject = accountSubject;
    this.preferences = preferences;
    this.scheduler = scheduler;
  }

  @Override public Observable<AptoideAccount> getAccount() {
    return accountSubject.startWith(getPreferencesAccount())
        .subscribeOn(scheduler);
  }

  @SuppressLint("ApplySharedPref") @Override public Completable save(AptoideAccount account) {
    return Completable.fromAction(() -> preferences.edit()
        .putBoolean(IS_LOGGED_IN, account.isLoggedIn())
        .putBoolean(HAS_STORE, account.hasStore())
        .putString(STORE_NAME, account.getStoreName())
        .commit())
        .doOnComplete(() -> accountSubject.onNext(account))
        .subscribeOn(scheduler);
  }

  @SuppressLint("ApplySharedPref") @Override public Completable remove() {
    return Completable.fromAction(() -> preferences.edit()
        .clear()
        .commit())
        .subscribeOn(scheduler);
  }

  private AptoideAccount getPreferencesAccount() {
    return new AptoideAccount(preferences.getBoolean(HAS_STORE, false),
        preferences.getBoolean(IS_LOGGED_IN, false), preferences.getString(STORE_NAME, null));
  }
}
