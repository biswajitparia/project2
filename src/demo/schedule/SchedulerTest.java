package demo.schedule;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchedulerTest {
    @Test
    public void test_scheduler() {
        Consumer<Long> handler = a -> System.out.println("Remote handler is called: " + a);
        Scheduler scheduler = new Scheduler();

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(() -> {
            scheduler.schedule(1691850722020L);
            scheduler.schedule(1691850722024L);//
            scheduler.schedule(1691850722025L);
            scheduler.poll(1691850722024L, handler, 3);
        });

        executorService.execute(() -> {
            scheduler.schedule(1691850722018L);
            scheduler.schedule(1691850722019L);
            scheduler.poll(1691850722023L, handler, 5);
        });

        executorService.execute(() -> {
            scheduler.schedule(1691850722021L);
            scheduler.schedule(1691850722022L);
            scheduler.schedule(1691850722023L);
            scheduler.cancel(1691850722024L);
            scheduler.poll(1691850722022L, handler, 8);
        });

        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(1, scheduler.size());


    }

}