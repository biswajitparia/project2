package demo.schedule;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Scheduler implements DeadlineEngine {
    public static final int INITIAL_CAPACITY = 1000;
    private final Queue<Long> events = new PriorityBlockingQueue<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public long schedule(long deadlineMs) {
        boolean flag = events.offer(deadlineMs);
        System.out.println("SCHEDULE: " + flag + " requestId: " + deadlineMs);
        return deadlineMs;
    }

    @Override
    public boolean cancel(long requestId) {
        boolean flag = false;
        try {
            lock.lock();

            flag = events.remove(requestId);
        } finally {
            lock.unlock();
        }
        System.out.println("CANCEL: " + flag + " requestId: " + requestId);
        return flag;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        int processedExpiredEvents = 0;
        try {
            lock.lock();
            while (events.peek() != null && events.peek() <= nowMs && maxPoll-- > 0) {
                handler.accept(events.poll());
                processedExpiredEvents++;
                System.out.println("POLL: maxPoll: " + maxPoll + " processedExpiredEvents: " + processedExpiredEvents);
            }
        } catch (Exception ex) {
            System.out.println("Error processing callback function");
        } finally {
            lock.unlock();
        }
        return processedExpiredEvents;
    }

    @Override
    public int size() {
        return events.size();
    }
}
