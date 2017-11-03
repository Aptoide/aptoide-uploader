package com.aptoide.uploader;

import io.reactivex.Single;

/**
 * Created by jdandrade on 03/11/2017.
 */

public interface AccountService {

  Single<AptoideAccount> getAccount(String username, String password);
}
