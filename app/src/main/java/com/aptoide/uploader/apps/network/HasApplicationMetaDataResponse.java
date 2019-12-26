package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.ResponseV7;
import com.squareup.moshi.Json;
import java.util.List;

/**
 * Created by filipegoncalves on 5/11/18.
 */

public class HasApplicationMetaDataResponse extends ResponseV7 {

  @Json(name = "data") private Data datast;

  public HasApplicationMetaDataResponse(Info info, List<Error> errors) {
    super(info, errors);
  }

  public Data getData() {
    return datast;
  }

  public void setData(Data data) {
    this.datast = data;
  }

  public static class Data {

    @Json(name = "exists") private boolean hasMetaData;

    public Data() {
    }

    public boolean hasMetaData() {
      return hasMetaData;
    }

    public void setHasMetaData(boolean hasMetaData) {
      this.hasMetaData = hasMetaData;
    }
  }
}
