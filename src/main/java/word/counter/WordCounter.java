package word.counter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class WordCounter {
    
    private static int MAX_CONSUMERS = 10;
    private ExecutorService executorService;

    public WordCounter(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public List<CompletableFuture<Void>> wordCountCFs(Map<String, AtomicLong> wordMap, BlockingQueue<String> blockingQueue) {
        System.out.println("Word counting file started...");
        List<CompletableFuture<Void>> cfsCount = new ArrayList<>();
        for (int i = 0; i < MAX_CONSUMERS; i++) {
            cfsCount.add(CompletableFuture.runAsync(() -> {
                processWordCounting(wordMap, blockingQueue);
            }, executorService));
        }
        return cfsCount;
    }

    private void processWordCounting(Map<String, AtomicLong> wordMap, BlockingQueue<String> blockingQueue) {
        try {
            while (true) {
                String line = blockingQueue.poll(30, TimeUnit.MILLISECONDS);
                if (line == null)
                    break;
                countWords(wordMap, line);
            }
        }
        catch (InterruptedException e) {
            System.out.println("Program interrupted..." + e.getMessage());
            e.printStackTrace();
        }
    }

    private void countWords(Map<String, AtomicLong> wordMap, String line) {
        line = line.replaceAll("\\p{Punct}", "").toLowerCase();
        Arrays.stream(line.split("\\s+")).forEach(w -> {
            if (w.length() != 0) {
                AtomicLong count = wordMap.get(w);
                if (count == null) {
                    count = new AtomicLong(0);
                    AtomicLong oldVal = wordMap.putIfAbsent(w, count);
                    if (oldVal != null) {
                        count = oldVal;
                    }
                }
                count.incrementAndGet();
            }
        });
    }
}
