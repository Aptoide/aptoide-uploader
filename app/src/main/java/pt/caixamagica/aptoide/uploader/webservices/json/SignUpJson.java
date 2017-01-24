/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;
import lombok.Data;

/**
 * Created by neuro on 14-04-2015.
 */
@Data public class SignUpJson {

  private String status;
  private List<Error> errors;
}
