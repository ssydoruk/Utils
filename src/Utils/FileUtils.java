/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.File;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author loaner
 */
public class FileUtils {

    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;  // Boolean indicating whether directory was set
        File directory;       // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs()) {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return result;
    }

    public static void mkDir(String directory_name) {
        LogManager.getLogger().debug("mkdir [" + directory_name + "]");
        File directory = new File(directory_name).getAbsoluteFile();
        if (!directory.exists()) {
            boolean mkdirs = directory.mkdirs();
            LogManager.getLogger().debug("directory " + " created: " + mkdirs);
        }

    }
}
