/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Utils.UnixProcess.IDoneFileAction;
import Utils.UnixProcess.IProgress;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
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

    private static HashMap<FilterStr, FileNameExtensionFilter> chooserFilters = null;

    private static HashMap<String, JFileChooser> storedChoosers = null;

    public static File selectSingleFile(Component parent, File currDir, String title, String descr, String... ext) {
        if (storedChoosers == null) {
            storedChoosers = new HashMap<>();
        }

        String filterFiles = (descr != null && !descr.isEmpty()) ? descr : ((ext.length > 0 && ext[0] != null && !ext[0].isEmpty()) ? getAll(ext) : "");
        String chooserHash = String.valueOf(parent.hashCode()) + filterFiles;
        JFileChooser chooser = storedChoosers.get(chooserHash);
        if (chooser == null) {
            chooser = new JFileChooser();
            storedChoosers.put(chooserHash, chooser);
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

    private static String getAll(String[] ext) {
        StringBuilder ret = new StringBuilder();
        for (String string : ext) {
            if (string != null) {
                ret.append(string);
            }
        }
        return ret.toString();
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

    public static void zipFile(Path destFile, IDoneFileAction doneFileAction, IProgress progressProc) {
        String fn = destFile.toString();
        ArrayList<String> unzippedFiles = unzipFiles(fn, progressProc);
        for (String unzippedFile : unzippedFiles) {
            boolean success = false;
            try {
                String zipFile = doZipFile(unzippedFile);
                if (doneFileAction != null) {
                    doneFileAction.fileDone(Paths.get(zipFile));
                }
                success = true;
            } catch (IOException ex) {
                logger.error("failed to zip [" + destFile + "]");
            }
            if (success) {
                deleteIgnoreException(unzippedFile, progressProc);
            }
        }
    }

    public static ArrayList<String> unzipFiles(String fn, IProgress progressProc) {
        String ext = FilenameUtils.getExtension(fn).toLowerCase();

        ArrayList<String> ret = new ArrayList();
        if (ext.equals("gz")) {
            String target = FilenameUtils.removeExtension(fn);
            if (FilenameUtils.getExtension(target).toLowerCase().equals("tar")) {
                ArrayList<String> tmpList = unzipTGZ(target, progressProc);
                if (tmpList != null) {
                    for (String string : tmpList) {
                        ret.addAll(unzipFiles(string, progressProc));
                    }
                }
                return ret;
            } else {
                boolean success = false;
                try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(fn))) {
                    Files.copy(gis, Paths.get(target), REPLACE_EXISTING);
                    ret.addAll(unzipFiles(target, progressProc));
                    success = true;
                } catch (FileNotFoundException ex) {
                    if (progressProc != null) {
                        progressProc.inform("Exception: " + ex.getMessage());
                    }
                } catch (IOException ex) {
                    if (progressProc != null) {
                        progressProc.inform("Exception: " + ex.getMessage());
                    }
                }
                if (success) {
                    deleteIgnoreException(fn, progressProc);
                }

                return ret;
            }
        } else if (ext.equals("tgz")) {
            ArrayList<String> tmpList = unzipTGZ(fn, progressProc);
            if (tmpList != null) {
                for (String string : tmpList) {
                    ret.addAll(unzipFiles(string, progressProc));
                }
            }
            return ret;
        } else if (ext.equals("zip")) {
            ret.add(fn);
            return ret;
        }
        ret.add(fn);
        return ret;
    }

    public static ArrayList<String> unzipTGZ(String target, IProgress progressProc) {
        ArrayList<String> ret = new ArrayList<>();
        String targetPath = FilenameUtils.getFullPath(target);
        boolean success = false;
        try (InputStream fi = Files.newInputStream(Paths.get(target)); BufferedInputStream bi = new BufferedInputStream(fi); GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi); TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {

                // create a new path, remember check zip slip attack
                Path newPath = zipSlipProtect(entry, Paths.get(targetPath));

                //checking
                // copy TarArchiveInputStream to newPath
                Files.copy(ti, newPath);
                ret.add(newPath.toString());
                success = true;
            }
        } catch (IOException ex) {
            if (progressProc != null) {
                progressProc.inform("Exception: " + ex.getMessage());
            }
        }
        if (success) {
            deleteIgnoreException(target, progressProc);
        }
        return ret;
    }

    public static String doZipFile(String sourceFile) throws IOException {
        String fileName = FilenameUtils.getName(sourceFile);
        String dstArchive = sourceFile + ".zip";
        ZipEntry zipEntry = new ZipEntry(fileName);
        try (
                FileOutputStream fos = new FileOutputStream(dstArchive); ZipOutputStream zipOut = new ZipOutputStream(fos); FileInputStream fis = new FileInputStream(new File(sourceFile));) {
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[8192];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
        return dstArchive;
    }

    private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir)
            throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());

        // make sure normalized file still has targetDir as its prefix,
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad entry: " + entry.getName());
        }

        return normalizePath;
    }

    private static void deleteIgnoreException(String fn, IProgress progressProc) {
        try {
            Files.deleteIfExists(Paths.get(fn));
        } catch (IOException ex) {
            if (progressProc != null) {
                progressProc.inform("Exception deleting [" + fn + "]: " + ex.getMessage());
            }
        }
    }

    public static boolean isZIPArchive(File f) {
        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            fileSignature = raf.readInt();
        } catch (IOException e) {
            // handle if you like
        }
        return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    }

}
