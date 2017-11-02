package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;

/**
 * Created by filipe on 30-10-2017.
 */

public class CategoriesResponse {

  public Info info;
  public List<Error> errors;
  public DataList datalist;

  public Error getError() {
    if (errors != null && errors.size() > 0) {
      return errors.get(0);
    }
    return null;
  }

  @lombok.Data public static class Info {
    private Status status;
    private Time time;

    public enum Status {
      OK, QUEUED, FAIL
    }

    @lombok.Data public static class Time {
      private Double seconds;
      private String human;
    }
  }

  @lombok.Data public static class Error {
    private String code;
    private String description;
    private String details;
  }

  @lombok.Data public static class DataList {
    private Number total;
    private Number count;
    private Number offset;
    private Number limit;
    private Number next;
    private Number hidden;
    private boolean loaded;
    private java.util.List<List> list;

    /**
     * Represents a category - List is the name that was on the webservice
     */
    @lombok.Data public static class List {
      private Number id;
      private String name;
      private String title;
      private String icon;
      private String graphic;
      private String added;
      private String modified;
      private Parent parent;
      private Stats stats;

      @lombok.Data public static class Parent {
        private Number id;
        private String name;
        private String title;
        private String icon;
        private String graphic;
      }

      @lombok.Data public static class Stats {
        private Number groups;
        private Number items;
      }

      @Override public boolean equals(Object o) {
        if (o == this) {
          return true;
        }

        if (o instanceof CategoriesResponse.DataList.List) {
          return ((CategoriesResponse.DataList.List) o).getId()
              .equals(id);
        }

        return super.equals(o);
      }
    }
  }
}
