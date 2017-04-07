package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;

/**
 * Created by pedroribeiro on 03/04/17.
 */

public class GetProposedResponse {

  public Info info;
  public List<Error> errors;
  public List<Data> data;

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

    @lombok.Data public static class Error {
      private String code;
      private String description;
    }
  }

  @lombok.Data public static class Data {
    private String language;
    private String title;
    private String description;
    private String news;
  }
}
