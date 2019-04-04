
package word.counter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class WordCountUtil {

    private int FILE_CORE_POOL_SIZE = 3;
    private int FILE_MAX_POOL_SIZE = 5;
    private int FILE_KEEP_ALIVE_TIME = 500;

    private int WC_CORE_POOL_SIZE = 5;
    private int WC_MAX_POOL_SIZE = 5;
    private int WC_KEEP_ALIVE_TIME = 500;
    
    private final ConcurrentHashMap<String, AtomicLong> wordCountMap;
    private final BlockingQueue<String> blockingQueue;
    
    private final ExecutorService fileExecutor;
    private final ExecutorService wordCountExecutor;
    
    public WordCountUtil() {
        wordCountMap = new ConcurrentHashMap<String, AtomicLong>();
        blockingQueue = new ArrayBlockingQueue<>(100);
        fileExecutor = getFileExecutorService();
        wordCountExecutor = getWordCountExecutorService();
    }

    public Map<String, AtomicLong> process(List<String> files) {
        long startTime = System.currentTimeMillis();
        FilesReader fileProcessor = new FilesReader(fileExecutor);
        List<CompletableFuture<Void>> cfs = fileProcessor.readAndAddtoBQ(files, blockingQueue);
        
        WordCounter wordCounter = new WordCounter(wordCountExecutor);
        List<CompletableFuture<Void>> cfsCount = wordCounter.wordCountCFs(wordCountMap, blockingQueue);
        
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).join();
        fileExecutor.shutdownNow();
        
        CompletableFuture.allOf(cfsCount.toArray(new CompletableFuture[cfsCount.size()])).join();
        wordCountExecutor.shutdown();
        
        System.out.println("Computation Time in MS: "+(System.currentTimeMillis()-startTime));
        return wordCountMap;
    }
    
    private ExecutorService getFileExecutorService() {
        return new ThreadPoolExecutor(FILE_CORE_POOL_SIZE, FILE_MAX_POOL_SIZE, FILE_KEEP_ALIVE_TIME, 
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
    }
    private ExecutorService getWordCountExecutorService() {
        return new ThreadPoolExecutor(WC_CORE_POOL_SIZE, WC_MAX_POOL_SIZE, WC_KEEP_ALIVE_TIME, 
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
    }
    
}
