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

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

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
        logger.debug("mkdir [" + directory_name + "]");
        File directory = new File(directory_name).getAbsoluteFile();
        if (!directory.exists()) {
            boolean mkdirs = directory.mkdirs();
            logger.debug("directory " + " created: " + mkdirs);
        }

    }
}
