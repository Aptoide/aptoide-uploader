/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.io.Serializable;
import lombok.ToString;

/**
 * Created by rmateus on 03-01-2014.
 */
@ToString public class Error implements Serializable {

  private String code;

  private String msg;

  public String getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
}
