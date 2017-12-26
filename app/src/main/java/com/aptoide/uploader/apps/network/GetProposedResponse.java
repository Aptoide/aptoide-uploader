package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.ResponseV7;
import java.util.List;

/**
 * Created by filipe on 26-12-2017.
 */

public class GetProposedResponse extends ResponseV7 {

  public List<Data> data;

  public GetProposedResponse(Info info, List<Error> errors) {
    super(info, errors);
  }

  public List<Data> getData() {
    return data;
  }

  public static class Data {
    public String getLanguage() {
      return language;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public String getNews() {
      return news;
    }

    private String language;
    private String title;
    private String description;
    private String news;
  }

  public boolean hasErrors() {
    return getError() == null;
  }

  public Info getInfo() {
    return getInfo();
  }

  public boolean requestFailed() {
    return getInfo().getStatus()
        .equals(Info.Status.FAIL);
  }
}

