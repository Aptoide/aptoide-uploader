package com.aptoide.uploader.account.network;

import java.util.List;

/**
 * Created by franciscoaleixo on 03/11/2017.
 */

public class AccountResponse extends ResponseV7 {
  private Nodes nodes;

  public AccountResponse(Nodes nodes, Info info, List<Error> errors) {
    super(info, errors);
    this.nodes = nodes;
  }

  public Nodes getNodes() {
    return nodes;
  }

  public static class Nodes {
    private GetUserMeta meta;

    public Nodes(GetUserMeta meta) {
      this.meta = meta;
    }

    public GetUserMeta getMeta() {
      return meta;
    }
  }

  public static class GetUserMeta {
    private Data data;

    public GetUserMeta(Data data) {
      this.data = data;
    }

    public Data getData() {
      return data;
    }

    public static class Data {
      private String avatar;

      private Store store;

      public Data(String avatar, Store store) {
        this.avatar = avatar;
        this.store = store;
      }

      public String getAvatar() {
        return avatar;
      }

      public Store getStore() {
        return store;
      }
    }
  }

  public static class Store {
    private String name;
    private String avatar;
    private long id;

    public Store(String name, String avatar, long id) {
      this.name = name;
      this.avatar = avatar;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public String getAvatar() {
      return avatar;
    }

    public long getId() {
      return id;
    }
  }
}
