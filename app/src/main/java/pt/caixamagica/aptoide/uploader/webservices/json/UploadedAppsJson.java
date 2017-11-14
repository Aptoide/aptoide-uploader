package pt.caixamagica.aptoide.uploader.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Created by filipe on 26-10-2017.
 */

public class UploadedAppsJson {

  public Info info;
  public java.util.List<Error> errors;
  public DataList datalist;

  public Error getError() {
    if (errors != null && errors.size() > 0) {
      return errors.get(0);
    }
    return null;
  }

  @lombok.Data public static class Info {
    private Info.Status status;
    private Info.Time time;

    public enum Status {
      OK, QUEUED, FAIL
    }

    @lombok.Data public static class Time {
      private double seconds;
      private String human;
    }
  }

  @lombok.Data public static class Error {
    private String code;
    private String description;
    private String details;
  }

  @lombok.Data public static class DataList {
    private int total;
    private int count;
    private int offset;
    private int limit;
    private int next;
    private int hidden;
    private boolean loaded;
    private List<App> list;

    @lombok.Data public static class App {
      private long id;
      private String name;
      private long size;
      private String icon;
      private String graphic;
      private String status;
      private String mode;
      private String added;
      private String modified;
      private String updated;
      @JsonProperty("package") private Package appPackage;
      private File file;

      @lombok.Data public static class Package {
        private String status;
        private String added;
        private String modified;
      }

      @lombok.Data public static class File {
        private String vername;
        private int vercode;
        private String md5sum;
        private String status;
        @JsonProperty("package") private Package packageName;

        @lombok.Data public static class Package {
          private String name;
          private String status;
        }
      }
    }
  }
}
