package demo.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class CacheManager<K, V> implements Cache<K, V> {
    private final Map<K, V> localCache = new HashMap<>();
    private final Function<K, V> externalCache;
    private final ReentrantLock lock = new ReentrantLock();

    public CacheManager(Function<K, V> externalCache) {
        this.externalCache = externalCache;
    }

    public V get(K key) {
        try {
            // Retrieve value if key is present in local cache
            if (localCache.containsKey(key)) {
                V localCacheVal = localCache.get(key);
                System.out.println("localCache: key: " + key + " value: " + localCacheVal);
                return localCacheVal;
            }
            //take a lock
            lock.lock();

            // Try to retrieve value again in case this key and corresponding value gets added by other thread using external call earlier
            if (localCache.containsKey(key)) {
                V localCacheValAddedByOtherThread = localCache.get(key);
                System.out.println("localCacheValAddedByOtherThread: key: " + key + " value: " + localCacheValAddedByOtherThread);
                return localCacheValAddedByOtherThread;
            }

            // External call is made when key is not found in local cache
            V externalVal = externalCache.apply(key);
            System.out.println("externalCache: key: " + key + " value: " + externalVal);
            // populate local cache
            localCache.put(key, externalVal);
            return externalVal;
        } catch (Exception ex) {
            System.out.println("Error fetching external cache:  " + ex);
            // populate local cache value to null
            localCache.put(key, null);
            return null;
        } finally {
            //release lock
            lock.unlock();
        }
    }

    public int sizeOfLocalCache(){
        return localCache.size();
    }
}
