/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public abstract class FileWatcher {

    public static void main(String[] args) throws InterruptedException, IOException {
        FileWatcher fw = (new FileWatcher(new File("/Users/stepan_sydoruk/aa.txt")) {
            @Override
            public void doOnChange(File f) {
                System.out.println("file changed " + f.getAbsolutePath());
            }
        }).watch();
        Thread.sleep(1000000);

    }

//    final static Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private final File file;
    private long pollTimeMS = 25;
    private final FileWatcherThread fwt;

    public FileWatcher(File file) throws IOException {
        this.file = file.getCanonicalFile();
        fwt = new FileWatcherThread();
    }

    public FileWatcher(File file, long _pollTimeMS) throws IOException {
        this(file);
        pollTimeMS = _pollTimeMS;
    }

    public abstract void doOnChange(File f);

    public FileWatcher watch() {
        fwt.setDaemon(true);
        fwt.start();
        return this;
    }

    public void pause(boolean isPause) {
        fwt.setPause(isPause);
    }

    public void stopThread() {
        fwt.stopThread();
    }

    class FileWatcherThread extends Thread {

        private final AtomicBoolean stop = new AtomicBoolean(false);
        private final AtomicBoolean paused = new AtomicBoolean(false);

        public boolean isStopped() {
            return stop.get();
        }

        public void stopThread() {
            stop.set(true);
        }

        @Override
        public void run() {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                Path path = file.toPath().getParent();
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                while (!isStopped()) {
                    WatchKey key;
                    try {
                        key = watcher.poll(pollTimeMS, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (key == null) {
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
                                && filename.toString().equals(file.getName())) {
                            if (paused.get() == true) {
                                paused.set(false);
                            } else {
                                doOnChange(file);
                            }
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                }
            } catch (Throwable e) {
                // Log or rethrow the error
            }
        }

        private void setPause(boolean pause) {
            paused.set(pause);
        }

    }
}
