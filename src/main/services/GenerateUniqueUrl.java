package services;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class GenerateUniqueUrl {

    private static final int BATCH_SIZE = 100000;
    private static final int THRESHOLD = 5000;

    private static final char[] BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final ReentrantLock lock = new ReentrantLock(); // to ensure atomicity
    private long[] currentIdPool;
    private long[] nextPool;
    private int currentIndex = 0;

    // Imagine this as Redis auto-increment
    private long lastFetchedId = 0;
    private boolean isFetchingNext = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public GenerateUniqueUrl() {
        currentIdPool = new long[BATCH_SIZE];
        nextPool = new long[BATCH_SIZE];
        fetchNewBatch(currentIdPool);
    }

    public String getUniqueUrl() {
        lock.lock();
        try {

            if (currentIndex >= BATCH_SIZE) {
                // current pool exhausted
                if (nextPool[0] == 0) {
                    // If next pool is not ready, blocking fetch (shouldn't usually happen)
                    fetchNewBatch(nextPool);
                }
                swapPools();
            }

            // If close to threshold, start async prefetch
            if (currentIndex >= BATCH_SIZE - THRESHOLD && !isFetchingNext) {
                isFetchingNext = true;
                executorService.submit(this::prefetchNextPool);
            }

            long id = currentIdPool[currentIndex];
            currentIndex++;
            return encodeBase62(id);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void swapPools() {
        long[] temp = currentIdPool;
        currentIdPool = nextPool;
        nextPool = temp;
        currentIndex = 0;
        isFetchingNext = false; // allow future prefetch
        clear(nextPool); // reset old pool
    }

    private void clear(long[] pool) {
        for (int i = 0; i < pool.length; i++) {
            pool[i] = 0;
        }
    }

    private void prefetchNextPool() {
        long[] newPool = new long[BATCH_SIZE];
        fetchNewBatch(newPool);
        lock.lock();
        try {
            nextPool = newPool;
            isFetchingNext = false;
        } finally {
            lock.unlock();
        }
    }

    private void fetchNewBatch(long[] pool) {
        // 🛑 TODO: Replace this with real Redis INCR or service call
        long startValue = fetchFromRedis(); // get the starting number
        for (int i = 0; i < BATCH_SIZE; i++) {
            pool[i] = startValue + i;
        }
    }

    private long fetchFromRedis() {
        // 🛑 TODO: Implement real Redis INCR
        // For now, simulate with static counter
        return 1;
    }

    private String encodeBase62(long value) {
        StringBuilder result = new StringBuilder();
        while (value > 0) {
            int reminder = (int) (value % BASE62.length);
            result.append(BASE62[reminder]);
            value /= BASE62.length;
        }
        return result.toString();
    }

}
