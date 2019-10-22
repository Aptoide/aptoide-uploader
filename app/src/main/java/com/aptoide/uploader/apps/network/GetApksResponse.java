package com.aptoide.uploader.apps.network;

import com.aptoide.uploader.account.network.Error;
import com.aptoide.uploader.account.network.ResponseV7;
import com.squareup.moshi.Json;
import java.util.List;

public class GetApksResponse extends ResponseV7 {

  private DataList datalist;

  public GetApksResponse(ResponseV7.Info info, List<Error> errors, DataList datalist) {
    super(info, errors);
    this.datalist = datalist;
  }

  public DataList getDatalist() {
    return datalist;
  }

  public static class DataList {

    private int total;
    private int count;
    private int offset;
    private int limit;
    private int next;
    private int hidden;
    private boolean loaded;
    private List<Data> list;

    public DataList(int total, int count, int offset, int limit, int next, int hidden,
        boolean loaded, List<Data> list) {
      this.total = total;
      this.count = count;
      this.offset = offset;
      this.limit = limit;
      this.next = next;
      this.hidden = hidden;
      this.loaded = loaded;
      this.list = list;
    }

    public int getTotal() {
      return total;
    }

    public int getCount() {
      return count;
    }

    public int getOffset() {
      return offset;
    }

    public int getLimit() {
      return limit;
    }

    public int getNext() {
      return next;
    }

    public int getHidden() {
      return hidden;
    }

    public boolean isLoaded() {
      return loaded;
    }

    public List<Data> getList() {
      return list;
    }
  }

  public static class Data {
    private int id;
    private String name;
    private int size;
    private String icon;
    private String graphic;
    private String status;
    private String mode;
    private String added;
    private String modified;
    private String updated;
    @Json(name = "package") private Package aPackage;
    private File file;

    public Data(int id, String name, int size, String icon, String graphic, String status,
        String mode, String added, String modified, String updated, Package aPackage, File file) {
      this.id = id;
      this.name = name;
      this.size = size;
      this.icon = icon;
      this.graphic = graphic;
      this.status = status;
      this.mode = mode;
      this.added = added;
      this.modified = modified;
      this.updated = updated;
      this.aPackage = aPackage;
      this.file = file;
    }

    public File getFile() {
      return file;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getSize() {
      return size;
    }

    public String getIcon() {
      return icon;
    }

    public String getGraphic() {
      return graphic;
    }

    public String getStatus() {
      return status;
    }

    public String getMode() {
      return mode;
    }

    public String getAdded() {
      return added;
    }

    public String getModified() {
      return modified;
    }

    public String getUpdated() {
      return updated;
    }

    public Package getPackage() {
      return aPackage;
    }
  }

  public static class Package {
    private String status;
    private String added;
    private String modified;

    public Package(String status, String added, String modified) {
      this.status = status;
      this.added = added;
      this.modified = modified;
    }

    public String getStatus() {
      return status;
    }

    public String getAdded() {
      return added;
    }

    public String getModified() {
      return modified;
    }
  }

  public static class File {
    private String vername;
    private String vercode;
    private String md5sum;
    private String status;
    @Json(name = "package") private FilePackage bPackage;

    public File(String vername, String vercode, String md5sum, String status,
        FilePackage aPackage) {
      this.vername = vername;
      this.vercode = vercode;
      this.md5sum = md5sum;
      this.status = status;
      this.bPackage = aPackage;
    }

    public String getVername() {
      return vername;
    }

    public String getVercode() {
      return vercode;
    }

    public String getMd5sum() {
      return md5sum;
    }

    public String getStatus() {
      return status;
    }

    public FilePackage getaPackage() {
      return bPackage;
    }
  }

  public static class FilePackage {
    private String name;
    private String status;

    public FilePackage(String name, String status) {
      this.name = name;
      this.status = status;
    }

    public String getName() {
      return name;
    }

    public String getStatus() {
      return status;
    }
  }
}
