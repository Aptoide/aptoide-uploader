package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.ResponseV7;
import com.squareup.moshi.Json;
import java.util.List;

public class GenericDraftResponse extends ResponseV7 {
  public GenericDraftResponse(Info info, List<Error> errors) {
    super(info, errors);
  }

  public Data data;

  public static class Data {
    @Json(name = "draft_id") private int draftId;
    @Json(name = "status") private String status;
    @Json(name = "errors") private List<Error> errors;

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public int getDraftId() {
      return draftId;
    }

    public void setDraftId(int draftId) {
      this.draftId = draftId;
    }

    public List<Error> getError() {
      return errors;
    }

  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }
}
