/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import com.google.gson.GsonBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author stepan_sydoruk
 */
public class StringUtils {

    public static boolean matching(Pattern ptSection, String stringKey) {
        if (ptSection != null && stringKey != null) {
            Matcher matcher = ptSection.matcher(stringKey);
            if (matcher != null) {
                return matcher.find();
            }
        }
        return false;
    }

    static public String toJson(Object obj) {
        return (new GsonBuilder().setPrettyPrinting().disableInnerClassSerialization()).create().toJson(obj);
    }

    static public int CharOccurences(StringBuilder s, char c) {
        int ret = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                ret++;
            }
        }
        return ret;
    }

    static public int CountStrings(StringBuilder sipBuf, String search) {
        int ret = 0, idx = 0;

        while (true) {
            if ((idx = sipBuf.indexOf(search, idx)) >= 0) {
                ret++;
                idx += search.length();
            } else {
                break;
            }

        }
        return ret;
    }

    static public boolean isNumeric(String s) {
        if (s != null && !s.isEmpty()) {
            boolean hasDigit = false;
            for (char c : s.toCharArray()) {
                if (Character.isDigit(c)) {
                    hasDigit = true;
                } else if (Character.isSpaceChar(c) || c == '.' || c == '-') {
                    continue;
                } else if (Character.isAlphabetic(c)) {
                    return false;
                }
            }
            if (hasDigit) {
                return true;
            }
        }
        return false;

    }
}
