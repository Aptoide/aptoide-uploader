package pt.caixamagica.aptoide.uploader.util;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by neuro on 03-10-2017.
 */

public class AlgorithmUtils {

  public static String computeMd5(@NonNull PackageInfo packageInfo) {

    String sourceDir = packageInfo.applicationInfo.sourceDir;
    File apkFile = new File(sourceDir);
    return computeMd5(apkFile);
  }

  public static String computeMd5(File f) {
    long time = System.currentTimeMillis();
    byte[] buffer = new byte[1024];
    int read, i;
    String md5hash;
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      InputStream is = new FileInputStream(f);
      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      md5hash = bigInt.toString(16);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    if (md5hash.length() != 33) {
      String tmp = "";
      for (i = 1; i < (33 - md5hash.length()); i++) {
        tmp = tmp.concat("0");
      }
      md5hash = tmp.concat(md5hash);
    }
    return md5hash;
  }
}
