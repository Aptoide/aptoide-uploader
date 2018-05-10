package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.ResponseV7;
import java.util.List;

/**
 * Created by filipe on 26-12-2017.
 */

public class GetProposedResponse extends ResponseV7 {

  private List<Data> data;

  public GetProposedResponse(Info info, List<Error> errors) {
    super(info, errors);
  }

  public List<Data> getData() {
    return data;
  }

  public static class Data {
    private String language;
    private String title;
    private String description;
    private String news;

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
  }
}

