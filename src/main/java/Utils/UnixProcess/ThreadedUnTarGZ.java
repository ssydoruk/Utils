/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

/**
 * @author stepansydoruk
 */
public class ThreadedUnTarGZ implements ThreadedOutputStreamReader {

    final static Logger logger = LoggerFactory.getLogger(ThreadedUnTarGZ.class);

    private final String targetDir;
    PipedOutputStream outputStream;
    PipedInputStream inputStream;

    public ThreadedUnTarGZ(String targetDir) throws IOException {
        this.targetDir = targetDir;

        outputStream = new PipedOutputStream();
        inputStream = new PipedInputStream();
        inputStream.connect(outputStream);
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
                logProgress("Starting "+entry.getName()+" dest: "+destFile.toString());
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
                    logProgress("Done file "+destFile);
                } catch (IOException e) {
                    logProgress("Failed to copy stream into [" + destFile + "]"+ e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logProgress("Thread job done");

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
}
