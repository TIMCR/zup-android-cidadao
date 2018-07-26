package br.com.lfdb.particity.util;

public class Strings {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isTrimmedNullOrEmpty(String s) {
        return isNullOrEmpty(s) || s.trim().length() == 0;
    }

    public static String nullToEmpty(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }
}
