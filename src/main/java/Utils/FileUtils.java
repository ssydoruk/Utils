/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
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

    private static String wd = null;

    public static String getCurrentDirectory() {
        if (wd == null) {
            wd = Paths.get(".").toAbsolutePath().normalize().toString();
        }
        return wd;
    }

    private static JFileChooser chooser = null;

    private static HashMap<FilterStr, FileNameExtensionFilter> chooserFilters;

    public static File selectSingleFile(Component parent, File currDir, String title, String descr, String... ext) {
        if (chooser == null) {
            chooser = new JFileChooser();
        }

        FileNameExtensionFilter filter;
        if (chooserFilters == null) {
            chooserFilters = new HashMap<>();
        }
        FilterStr filterRequest = new FilterStr(descr, ext);
        filter = chooserFilters.get(filterRequest);
        if (filter == null) {
            filter = new FileNameExtensionFilter(descr, ext);
            chooserFilters.put(filterRequest, filter);
        }
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        if (currDir != null) {
            chooser.setCurrentDirectory(currDir);
        }
        if (title != null) {
            chooser.setDialogTitle(title);
        }

        return (chooser.showOpenDialog(parent)
                == JFileChooser.APPROVE_OPTION)
                        ? chooser.getSelectedFile() : null;
    }

    public static void saveToFile(File theFile, String text) throws IOException {

        try (Writer writer
                = new BufferedWriter(
                        new FileWriter(theFile)
                )) {
            writer.write(text);
        }

    }

    static private class FilterStr extends Pair<String, String[]> {

        public FilterStr(String key, String... value) {
            super(key, value);
        }

        @Override
        public int hashCode() {
            return singleStr().hashCode(); //To change body of generated methods, choose Tools | Templates.
        }

        public String getDescr() {
            return super.getKey(); //To change body of generated methods, choose Tools | Templates.
        }

        public String[] getExt() {
            return super.getValue(); //To change body of generated methods, choose Tools | Templates.
        }

        private String singleStr() {
            return getKey() + org.apache.commons.lang3.StringUtils.join(getValue());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FilterStr other = (FilterStr) obj;
            return other.singleStr().equals(singleStr());
        }

    }

    public static String loadFile(File fileName) {
        StringBuilder txt = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String l;
            while ((l = reader.readLine()) != null) {
                txt.append(l).append('\n');

            }
        } catch (final FileNotFoundException ex) {
            logger.error("", ex);
        } catch (final IOException ex) {
            logger.error("", ex);
        }
        return txt.toString();
    }
}
