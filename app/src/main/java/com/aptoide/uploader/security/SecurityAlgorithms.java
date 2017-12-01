package com.aptoide.uploader.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This algorithms need to change to proper tested and open source implementations.
 */
@Deprecated
public class SecurityAlgorithms {

  private static final String ALGORITHM = "HmacSHA1";
  private static final String CHARSET_NAME = "UTF-8";
  private static final String CHARSET_NAME_2 = "iso-8859-1";
  private static final String ALGORITHM_2 = "SHA-1";

  private String convertToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        if ((0 <= halfbyte) && (halfbyte <= 9)) {
          buf.append((char) ('0' + halfbyte));
        } else {
          buf.append((char) ('a' + (halfbyte - 10)));
        }
        halfbyte = data[i] & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  public String calculateHash(String data)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance(ALGORITHM_2);
    md.update(data.getBytes(CHARSET_NAME_2), 0, data.length());
    byte[] digestedData = md.digest();
    return convertToHex(digestedData);
  }

  public String calculateIntegrityWithKey(List<String> fields, String key)
      throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException,
      NoSuchAlgorithmException {
    SecretKeySpec secretKeySpec = new SecretKeySpec((key).getBytes(CHARSET_NAME), ALGORITHM);
    Mac mac = Mac.getInstance(ALGORITHM);
    mac.init(secretKeySpec);

    StringBuilder stringBuilder = new StringBuilder();
    for (String field : fields) {
      if (field != null && !field.isEmpty()) {
        stringBuilder.append(field);
      }
    }
    String text = stringBuilder.toString();

    byte[] bytes = mac.doFinal(text.getBytes(CHARSET_NAME));
    return convertToHex(bytes);
  }
}
