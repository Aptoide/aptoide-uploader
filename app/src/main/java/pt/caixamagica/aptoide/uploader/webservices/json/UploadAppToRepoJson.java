/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import android.text.TextUtils;
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

  public String getErrorCode() {
    if (!TextUtils.isEmpty(error)) {
      return error;
    } else if (errors != null && !errors.isEmpty()) {
      return errors.get(0)
          .getCode();
    }
    return "No error";
  }

  public String getErrorDescription() {
    if (!TextUtils.isEmpty(error_description)) {
      return error_description;
    } else if (errors != null && !errors.isEmpty()) {
      return errors.get(0)
          .getMsg();
    }
    return "No description";
  }

  @Data public static class Info {

    private String iconUrl;
    private String url;
  }
}
