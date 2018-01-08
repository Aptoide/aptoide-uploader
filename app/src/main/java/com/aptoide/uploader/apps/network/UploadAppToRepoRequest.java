package com.aptoide.uploader.apps.network;

/**
 * Created by filipe on 27-12-2017.
 */

public class UploadAppToRepoRequest {

  private String token;
  private String repo;
  private String apkName;
  private String apkPath;
  private String packageName;
  private String description;
  private Integer category;
  private Integer rating;
  private String apkPhone;
  private String apkEmail;
  private String apkWebsite;
  private boolean onlyUserRepo;
  private String apkMd5sum;
  private int uploadType = 2;
  private String hmac;
  private String uploadFrom;
  private String obbMainPath;
  private String obbPatchPath;
  private String inputTitle = null;
  private String label;
  private String lang;
}
