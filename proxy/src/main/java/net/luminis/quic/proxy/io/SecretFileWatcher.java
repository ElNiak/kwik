package net.luminis.quic.proxy.io;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.IOException;

public class SecretFileWatcher implements Runnable {

    private String secretFilePath;
    private SecretUpdateListener listener;

    public SecretFileWatcher(String secretFilePath, SecretUpdateListener listener) {
        this.secretFilePath = secretFilePath;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            watchFile();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void watchFile() throws IOException, InterruptedException {
        Path path = Paths.get(secretFilePath).getParent();
        Path fileToWatch = Paths.get(secretFilePath).getFileName();

        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_MODIFY);

        while (true) {
            WatchKey key = watchService.take(); // Wait for a key to be available
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                if (kind == ENTRY_MODIFY && fileName.equals(fileToWatch)) {
                    // File has been modified
                    System.out.println("File has been modified: " + secretFilePath);
                    listener.onSecretFileModified(secretFilePath);
                }
            }

            // Reset the key -- this step is critical if you want to receive further watch events.
            boolean valid = key.reset();
            if (!valid) {
                break; // Exit loop if key is no longer valid
            }
        }
    }

    public interface SecretUpdateListener {
        void onSecretFileModified(String secretFilePath);
    }
}
