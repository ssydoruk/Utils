/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

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
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }
    public static Long intOrDef(String s, Long def) {
        if (s != null) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }

    public static Long intOrDef(String s, Long def, int radix) {
        if (s != null) {
            try {
                return Long.parseLong(s, radix);
            } catch (NumberFormatException e) {
            }
        }
        return def;

    }
}
