/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author loaner
 */
public class FileUtils {

    final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

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
