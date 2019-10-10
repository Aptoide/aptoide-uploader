package com.aptoide.uploader.account.persistence;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import com.aptoide.uploader.account.Account;
import com.aptoide.uploader.account.AccountFactory;
import com.aptoide.uploader.account.AccountPersistence;
import com.aptoide.uploader.account.BaseAccount;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;

public class SharedPreferencesAccountPersistence implements AccountPersistence {

  private static final String HAS_STORE = "HAS_STORE";
  private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
  private static final String STORE_NAME = "store_name";
  private static final String LOGIN_TYPE = "LOGIN_TYPE";
  private final PublishSubject<Account> accountSubject;
  private final SharedPreferences preferences;
  private final Scheduler scheduler;

  public SharedPreferencesAccountPersistence(PublishSubject<Account> accountSubject,
      SharedPreferences preferences, Scheduler scheduler) {
    this.accountSubject = accountSubject;
    this.preferences = preferences;
    this.scheduler = scheduler;
  }

  @Override public Observable<Account> getAccount() {
    return accountSubject.startWith(getPreferencesAccount())
        .subscribeOn(scheduler);
  }

  @SuppressLint("ApplySharedPref") @Override public Completable save(Account account) {
    return Completable.fromAction(() -> preferences.edit()
        .putBoolean(IS_LOGGED_IN, account.isLoggedIn())
        .putBoolean(HAS_STORE, account.hasStore())
        .putString(STORE_NAME, account.getStoreName())
        .putString(LOGIN_TYPE, account.getLoginType()
            .getText())
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

  private Account getPreferencesAccount() {
    return AccountFactory.of(preferences.getBoolean(HAS_STORE, false),
        preferences.getBoolean(IS_LOGGED_IN, false), preferences.getString(STORE_NAME, null),
        BaseAccount.LoginType.valueOf(
            preferences.getString(LOGIN_TYPE, BaseAccount.LoginType.NONE.name())));
  }
}
