package fileprocessor.word.counter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import word.counter.FilesReader;
import word.counter.WordCountUtil;
import word.counter.WordCounter;

public class WordCountUtilTest {
    
    @Test
    public void testReadAndAddtoBQ() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        BlockingQueue<String> testBq = new ArrayBlockingQueue<>(10);
        
        FilesReader fileProcessor = new FilesReader(executor);
        List<CompletableFuture<Void>> cfs = fileProcessor.readAndAddtoBQ(Arrays.asList("src/test/resources/test1.txt", "src/test/resources/test2.txt"), testBq);
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).join();
        assertEquals(4, testBq.size());
    }
    
    @Test
    public void testWordCount() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, AtomicLong>wordCountMap = new ConcurrentHashMap<String, AtomicLong>();
        BlockingQueue<String> testBq = getTestBQ();
        WordCounter wordCounter = new WordCounter(executor);
        List<CompletableFuture<Void>> cfs = wordCounter.wordCountCFs(wordCountMap, testBq);
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).join();
        assertEquals(9L, wordCountMap.size());
        assertEquals(2L, wordCountMap.get("these").longValue());
        assertEquals(2L, wordCountMap.get("things").longValue());
        assertEquals(4L, wordCountMap.get("dogs").longValue());
    }
    
    @Test
    public void testFileReadAndWordCount() {
        WordCountUtil wordCountUtil = new WordCountUtil();
        Map<String, AtomicLong> wordCountMap = wordCountUtil.process(Arrays.asList("src/test/resources/test1.txt", "src/test/resources/test2.txt"));
        assertNotNull(wordCountMap);
        assertEquals(9L, wordCountMap.size());
        assertEquals(2L, wordCountMap.get("these").longValue());
        assertEquals(2L, wordCountMap.get("things").longValue());
        assertEquals(4L, wordCountMap.get("dogs").longValue());
    }
    private BlockingQueue<String> getTestBQ() {
        BlockingQueue<String> testBq = new ArrayBlockingQueue<>(10);
        testBq.add("I like # % dogs. Dogs are cute.");
        testBq.add("Are these things^ like the others?");
        testBq.add("I like dogs. 'Dogs' are cute.");
        testBq.add("Are these things like the others?");
        return testBq;
    }
}
