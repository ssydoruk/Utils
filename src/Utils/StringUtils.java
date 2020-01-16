/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

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

}
