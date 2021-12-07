/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.io.*;
import org.slf4j.*;

/**
 * @author stepansydoruk
 */
public class ThreadedUnTarGZ implements ThreadedOutputStreamReader {

    final static Logger logger = LoggerFactory.getLogger(ThreadedUnTarGZ.class);

    private final String targetDir;
    PipedOutputStream outputStream;
    PipedInputStream inputStream;
    private final boolean zipDest;

    private IDoneFileAction doneFileAction=null;

    public void setDoneFileAction(IDoneFileAction doneFileAction) {
        this.doneFileAction = doneFileAction;
    }

    public ThreadedUnTarGZ(String targetDir, boolean isZipDest) throws IOException {
        this.targetDir = targetDir;

        this.zipDest = isZipDest;
        outputStream = new PipedOutputStream();
        inputStream = new PipedInputStream();
        inputStream.connect(outputStream);
    }

    public ThreadedUnTarGZ(String targetDir) throws IOException {
        this(targetDir, false);
    }

    private IProcessOutputRead progressProc = null;

    @Override
    public void run() {
        try (BufferedInputStream bi = new BufferedInputStream(inputStream);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                // create a new path, remember check zip slip attack
                Path destFile = Paths.get(targetDir, entry.getName());
                logProgress("Starting " + entry.getName() + " dest: " + destFile.toString());
                try {
                    if (Files.exists(destFile)) {
                        logProgress("File [" + destFile + "] exists. Removing");
                        Files.delete(destFile);
                    }
                } catch (IOException e) {
                    logProgress("Exception deleting file [" + destFile + "]: " + e);
                }
                //checking

                // copy TarArchiveInputStream to newPath
                try {
                    // todo: update new files with the same creation timestamp
                    // todo: download into temp file and then rename
                    // todo: logging of download process into main window
                    // todo: check entry size vs what actually transferred and print error
                    new File(FilenameUtils.getFullPath(destFile.toAbsolutePath().toString())).mkdirs();
                    Files.copy(ti, destFile);
                    Files.setLastModifiedTime(destFile, FileTime.fromMillis(entry.getLastModifiedDate().getTime()));
                    if (Files.size(destFile) != entry.getSize()) {
                        logProgress("!!! original and dest file sizes are different");
                    }
                    logProgress("Done file " + destFile);
                    if (zipDest) {
                        zipFile(destFile);
                    }
                    else {
                        doneFileAction(destFile);
                    }
                } catch (IOException e) {
                    logProgress("Failed to copy stream into [" + destFile + "]" + e);
                }
            }
        } catch (IOException e) {
            logProgress("Exception: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logProgress("Exception: " + e.getMessage());
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                logProgress("Exception: " + e.getMessage());
            }
        }
        logProgress("Thread job done");

    }

    private void doneFileAction(Path destFile) {
        if(doneFileAction!=null)
            doneFileAction.fileDone(destFile);
    }

    private void logProgress(String s) {
        System.out.println(s);
        if (progressProc != null) {
            progressProc.lineRead(s);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public IProcessOutputRead getProgressProc() {
        return progressProc;
    }

    public void setProgressProc(IProcessOutputRead progressProc) {
        this.progressProc = progressProc;
    }

    private void zipFile(Path destFile) {
        String fn = destFile.toString();
        ArrayList<String> unzippedFiles = unzipFiles(fn);
        for (String unzippedFile : unzippedFiles) {
            boolean success = false;
            try {
                String zipFile = doZipFile(unzippedFile);
                doneFileAction(Paths.get(zipFile));
                success = true;
            } catch (IOException ex) {
                logger.error("failed to zip [" + destFile + "]");
            }
            if (success)
                deleteIgnoreException(unzippedFile);
        }
    }

    /**
     * Unzip to raw files recoursively
     *
     * @param destFile
     * @return
     */
    private ArrayList<String> unzipFiles(String fn) {
        String ext = FilenameUtils.getExtension(fn).toLowerCase();

        ArrayList<String> ret = new ArrayList();
        if (ext.equals("gz")) {
            String target = FilenameUtils.removeExtension(fn);
            if (FilenameUtils.getExtension(target).toLowerCase().equals("tar")) {
                ArrayList<String> tmpList = unzipTGZ(target);
                if (tmpList != null) {
                    for (String string : tmpList) {
                        ret.addAll(unzipFiles(string));
                    }
                }
                return ret;
            } else {
                boolean success = false;
                try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(fn))) {
                    Files.copy(gis, Paths.get(target), REPLACE_EXISTING);
                    ret.addAll(unzipFiles(target));
                    success = true;
                } catch (FileNotFoundException ex) {
                    logProgress("Exception: " + ex.getMessage());
                } catch (IOException ex) {
                    logProgress("Exception: " + ex.getMessage());
                }
                if (success)
                    deleteIgnoreException(fn);

                return ret;
            }
        } else if (ext.equals("tgz")) {
            ArrayList<String> tmpList = unzipTGZ(fn);
            if (tmpList != null) {
                for (String string : tmpList) {
                    ret.addAll(unzipFiles(string));
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

    private void deleteIgnoreException(String fn) {
        try {
            Files.deleteIfExists(Paths.get(fn));
        } catch (IOException ex) {
            logProgress("Exception deleting [" + fn + "]: " + ex.getMessage());
        }
    }

    private ArrayList<String> unzipTGZ(String target) {
        ArrayList<String> ret = new ArrayList<>();
        String targetPath = FilenameUtils.getFullPath(target);
        boolean success=false;
        try (InputStream fi = Files.newInputStream(Paths.get(target));
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {

                // create a new path, remember check zip slip attack
                Path newPath = zipSlipProtect(entry, Paths.get(targetPath));

                //checking
                // copy TarArchiveInputStream to newPath
                Files.copy(ti, newPath);
                ret.add(newPath.toString());
                success=true;
            }
        } catch (IOException ex) {
            logProgress("Exception: " + ex.getMessage());
        }
        if(success)
            deleteIgnoreException(target);
        return ret;
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

    private String doZipFile(String sourceFile) throws FileNotFoundException, IOException {
        String fileName = FilenameUtils.getName(sourceFile);
        String dstArchive = sourceFile + ".zip";
        ZipEntry zipEntry = new ZipEntry(fileName);
        try (
                FileOutputStream fos = new FileOutputStream(dstArchive);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(new File(sourceFile));) {
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[8192];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
        return dstArchive;
    }

    public static void main(String[] args) throws IOException {
        ThreadedUnTarGZ t = new ThreadedUnTarGZ("C:\\GCTI\\work\\test\\ConfigProxy_FTW\\IL3PWGENURS1\\d$\\Logs\\ConfigProxy_FTW\\");
        t.zipFile(Paths.get("C:\\GCTI\\work\\test\\ConfigProxy_FTW\\IL3PWGENURS1\\d$\\Logs\\ConfigProxy_FTW\\arc.tgz"));
    }
}
