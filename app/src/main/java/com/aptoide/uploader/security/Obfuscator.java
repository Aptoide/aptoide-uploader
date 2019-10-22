package com.aptoide.uploader.security;

/**
 * Interface used as part of a {@link } to allow application authors to obfuscate licensing
 * data that will be stored into a SharedPreferences file.
 * <p>
 * Any transformation scheme must be reversable. Implementing classes may optionally implement an
 * integrity check to further prevent modification to preference data. Implementing
 * classes should use device-specific information as a key in the obfuscation algorithm to prevent
 * obfuscated preferences from being shared among devices.
 */
public interface Obfuscator {

  /**
   * Obfuscate a string that is being stored into shared preferences.
   *
   * @param original The data that is to be obfuscated.
   * @param key The key for the data that is to be obfuscated.
   *
   * @return A transformed version of the original data.
   */
  String obfuscate(String original, String key);

  /**
   * Undo the transformation applied to data by the obfuscate() method.
   *
   * data that is to be obfuscated.
   *
   * @param key The key for the data that is to be obfuscated.
   *
   * @return A transformed version of the original data.
   *
   * @throws ValidationException Optionally thrown if a data integrity check fails.
   */
  String unobfuscate(String obfuscated, String key) throws ValidationException;
}
