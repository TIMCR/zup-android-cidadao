package br.com.lfdb.particity.core;

import com.twitter.sdk.android.Twitter;

public class Crashlytics {

  private static String userIdentifier;
  private static String userEmail;
  private static Twitter instance;

  public static void setUserIdentifier(String userIdentifier) {
    Crashlytics.userIdentifier = userIdentifier;
  }

  public static String getUserIdentifier() {
    return userIdentifier;
  }

  public static void setUserEmail(String userEmail) {
    Crashlytics.userEmail = userEmail;
  }

  public static String getUserEmail() {
    return userEmail;
  }

  public static void logException(Exception e) {
    try {
      throw new Exception(e.getMessage());
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  public static Twitter getInstance() {
    return instance;
  }

  public static void setInstance(Twitter instance) {
    Crashlytics.instance = instance;
  }

  public static void setLong(String user, long userId) {

  }
}
