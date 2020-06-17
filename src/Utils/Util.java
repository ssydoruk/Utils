/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author stepansydoruk
 */
public class Util {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };// Operating systems.

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }

    public static Integer intOrDef(String s, Integer def) {
        if (StringUtils.isNotBlank(s)) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }

    public static Long intOrDef(String s, Long def) {
        if (StringUtils.isNotBlank(s)) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }

    public static <T> T defIfNull(T val, T def) {
        return (val == null) ? val : def;
    }


    public static Long intOrDef(String s, Long def, int radix) {
        if (StringUtils.isNotBlank(s)) {
            try {
                return Long.parseLong(s, radix);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }

    static public boolean matchFound(String val, Pattern pt, String search, boolean matchWholeWordSelected) {
//        logger.debug("search cell:[" + val + "] [" + search + "] " + matchWholeWordSelected + " " + " " + pt + ": [" + val);
        if (val != null && !val.isEmpty()) {
//                            inquirer.inquirer.logger.debug("search cell:" + search + " " + matchWholeWordSelected + " " + " " +pt + ": [" + val);
            if (pt != null) {
                if (pt.matcher(val).find()) {
                    return true;
                }
            } else {
                if (matchWholeWordSelected) {
                    if (val.equalsIgnoreCase(search)) {
                        return true;
                    }
                } else {
                    if (val.toLowerCase().contains(search)) {
                        return true;
                    }

                }

            }
        }
        return false;
    }

    private static final Pattern fileBaseName = Pattern.compile("([^\\/]+)$");

    static public String stripDir(String fileName) {
        return org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator(fileName);

//        Matcher m;
//        if ((m = fileBaseName.matcher(fileName)).find()) {
//            return m.group(0);
//        }
//        return fileName;
    }

    public static ArrayList<String> rSyncAddClause(String fileName) {
        return rSyncAddClause(fileName, "/*");
    }

    public static ArrayList<String> rSyncAddClause(String fileName, String prefix) {
        ArrayList<String> ret = new ArrayList<String>();
        ret.add("-f");//--filter
        ret.add("+ " + ((prefix != null) ? prefix : "") + fileName);
        return ret;
    }

    private static final char quoteChars[] = {'\'', '"', ' '};

    private static boolean charIn(char c, char[] quoteChars) {
        int i = 0;
        for (; i < quoteChars.length; i++) {
            if (quoteChars[i] == c) {
                break;
            }
        }
        return i < quoteChars.length;
    }

    public static String StripQuotes(String UUId) {
        if (StringUtils.isNotBlank(UUId)) {
            int startI = 0;
            char[] toCharArray = UUId.toCharArray();
            for (int j = 0; j < toCharArray.length; j++) {
                if (!charIn(toCharArray[j], quoteChars)) {
                    startI = j;
                    break;
                }
            }
            for (int j = toCharArray.length - 1; j >= 0; j--) {
                if (!charIn(toCharArray[j], quoteChars)) {
                    return UUId.substring(startI, j + 1);
                }
            }
        }

        return UUId;
    }
}
