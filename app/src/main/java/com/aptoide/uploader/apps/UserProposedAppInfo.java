package com.aptoide.uploader.apps;

/**
 * Created by franciscoaleixo on 15/12/2017.
 */

public class UserProposedAppInfo {
    private String name;
    private int rating;
    private int category;
    private String language;
    private String description;
    private String phoneNr;
    private String email;
    private String website;

    public UserProposedAppInfo(String name, int rating, int category, String language, String description, String phoneNr, String email, String website) {
        this.name = name;
        this.rating = rating;
        this.category = category;
        this.language = language;
        this.description = description;
        this.phoneNr = phoneNr;
        this.email = email;
        this.website = website;
    }

    public UserProposedAppInfo(RemoteProposedAppInfo remoteProposedAppInfo){
        this.name = remoteProposedAppInfo.getAppName();
        this.category = remoteProposedAppInfo.getAppCategory();
        this.language = remoteProposedAppInfo.getLanguage();
    }

    public UserProposedAppInfo(RemoteProposedAppInfo remoteProposedAppInfo, int rating, String description, String phoneNr, String email, String website) {
        this.name = remoteProposedAppInfo.getAppName();
        this.category = remoteProposedAppInfo.getAppCategory();
        this.language = remoteProposedAppInfo.getLanguage();
        this.rating = rating;
        this.description = description;
        this.phoneNr = phoneNr;
        this.email = email;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public int getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }
}
