package com.aptoide.uploader.apps;

public class Obb {

  private String filename;
  private String md5sum;
  private String path;

  public Obb(String filename, String md5sum, String path) {
    this.filename = filename;
    this.md5sum = md5sum;
    this.path = path;
  }

  public String getFilename() {
    return this.filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMd5sum() {
    return this.md5sum;
  }

  public void setMd5sum(String md5sum) {
    this.md5sum = md5sum;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}