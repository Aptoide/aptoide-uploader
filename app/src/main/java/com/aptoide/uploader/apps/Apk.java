package com.aptoide.uploader.apps;

import com.aptoide.uploader.apps.network.GetApksResponse;

public class Apk {

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
  private GetApksResponse.Package aPackage;
  private GetApksResponse.File file;

  public Apk(int id, String name, int size, String icon, String graphic, String status, String mode,
      String added, String modified, String updated, GetApksResponse.Package aPackage,
      GetApksResponse.File file) {
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

  public GetApksResponse.File getFile() {
    return file;
  }

  public void setFile(GetApksResponse.File file) {
    this.file = file;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getGraphic() {
    return graphic;
  }

  public void setGraphic(String graphic) {
    this.graphic = graphic;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getAdded() {
    return added;
  }

  public void setAdded(String added) {
    this.added = added;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getUpdated() {
    return updated;
  }

  public void setUpdated(String updated) {
    this.updated = updated;
  }

  public GetApksResponse.Package getPackage() {
    return aPackage;
  }

  public void setPackage(GetApksResponse.Package aPackage) {
    this.aPackage = aPackage;
  }

  public static class Package {
    private String status;
    private String added;
    private String modified;

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getAdded() {
      return added;
    }

    public void setAdded(String added) {
      this.added = added;
    }

    public String getModified() {
      return modified;
    }

    public void setModified(String modified) {
      this.modified = modified;
    }
  }

  public static class File {
    private String vername;
    private String vercode;
    private String md5sum;
    private GetApksResponse.FilePackage aPackage;

    public String getVername() {
      return vername;
    }

    public void setVername(String vername) {
      this.vername = vername;
    }

    public String getVercode() {
      return vercode;
    }

    public void setVercode(String vercode) {
      this.vercode = vercode;
    }

    public String getMd5sum() {
      return md5sum;
    }

    public void setMd5sum(String md5sum) {
      this.md5sum = md5sum;
    }

    public GetApksResponse.FilePackage getPackage() {
      return aPackage;
    }

    public void setPackage(GetApksResponse.FilePackage aPackage) {
      this.aPackage = aPackage;
    }
  }

  public static class FilePackage {
    private String name;
    private String status;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}
