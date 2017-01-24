/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;
import lombok.Data;

/**
 * Created by neuro on 22-02-2015.
 */
@Data public class UploadAppToRepoJson {

  private String status;
  private Info info;
  private String error;
  private String error_description;
  private List<Error> errors;

  @Data public static class Info {

    private String iconUrl;
    private String url;
  }
}
