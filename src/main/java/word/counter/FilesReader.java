package word.counter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


public class FilesReader {
    private ExecutorService executorService;

    public FilesReader(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public List<CompletableFuture<Void>> readAndAddtoBQ(List<String> fileNames, BlockingQueue<String> blockingQueue) {
        System.out.println("File reading started...");
        return fileNames.stream().map(fileName -> readCompleteableFuture(fileName, blockingQueue)).collect(Collectors.toList());
    }

    private CompletableFuture<Void> readCompleteableFuture(final String fileName, final BlockingQueue<String> blockingQueue) {
        return CompletableFuture.runAsync(() -> {
            readFileAndUpdateBq(fileName, blockingQueue);
        }, executorService);
    }

    private void readFileAndUpdateBq(String fileName, BlockingQueue<String> blockingQueue) {
        Path path = Paths.get(fileName);
        BufferedReader bufferedReader;
        try {
            bufferedReader = Files.newBufferedReader(path);
            String currentLine = bufferedReader.readLine();
            while (currentLine != null) {
                if (!"".equals(currentLine.trim())) {
                    blockingQueue.put(currentLine);
                }
                currentLine = bufferedReader.readLine();
            }
        }
        catch (IOException | InterruptedException e) {
            System.out.println("Error during file read..!" + e.getMessage());
        }
    }
}
