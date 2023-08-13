package demo.cache;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheManagerTest {
    @Test
    public void test_cache_manager() {
        Map<Integer, String> externalMap = new HashMap();
        externalMap.put(1, "one");
        externalMap.put(2, "two");
        externalMap.put(null, "VALUE_FOR_NULL_KEY");
        externalMap.put(5, null);

        Function<Integer, String> externalCache = (k) -> externalMap.get(k);

        CacheManager<Integer, String> cacheManager = new CacheManager<>(externalCache);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(() -> cacheManager.get(1));
        executorService.execute(() -> cacheManager.get(1));
        executorService.execute(() -> cacheManager.get(2));
        executorService.execute(() -> cacheManager.get(null));
        executorService.execute(() -> cacheManager.get(null));
        executorService.execute(() -> cacheManager.get(5));
        executorService.execute(() -> cacheManager.get(5));
        executorService.execute(() -> cacheManager.get(5));
        executorService.execute(() -> cacheManager.get(6));
        executorService.execute(() -> cacheManager.get(6));

        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(5, cacheManager.sizeOfLocalCache());

    }
}