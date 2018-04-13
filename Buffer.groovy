import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

/**
 * @author Jonatan Ivanov
 */
class Buffer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Buffer.class)

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>()
    private final AtomicInteger inProgressCounter = new AtomicInteger()

    private final String name
    private final Supplier<T> supplier
    private final int desiredSize

    // This should be absolutely not necessary, it is more like a hack to make the spring-boot-cli happy o.O
    private Buffer() {
        this(UUID.randomUUID().toString(),{ null }, 0)
    }

    Buffer(String name, Supplier<T> supplier, int desiredSize) {
        this.name = name
        this.supplier = supplier
        this.desiredSize = desiredSize
    }

    T poll() {
        return queue.poll()
    }

    void clear() {
        queue.clear()
    }

    int size() {
        return queue.size()
    }

    int inProgressCount() {
        return inProgressCounter.get()
    }

    def stats() {
        return [
                name: name,
                supplier: supplier.getClass().getSimpleName(),
                desiredSize: desiredSize,
                size: queue.size(),
                inProgress: this.inProgressCount()
        ]
    }

    @Override
    String toString() {
        return stats().toString()
    }

    synchronized void fill() {
        int currentSize = queue.size()
        int inProgress = inProgressCounter.get()
        int goal = desiredSize - currentSize - inProgress

        if (goal > 0) {
            LOGGER.info("buffer: $name, currentSize: $currentSize, inProgress: $inProgress, goal: $goal")
            for (int i = 0; i < goal; i++) {
                inProgressCounter.incrementAndGet()
                CompletableFuture.runAsync({ fetchAndAdd() })
            }
        }
    }

    private void fetchAndAdd() {
        try {
            queue.add(supplier.get())
        }
        catch (Exception e) {
            LOGGER.warn("buffer: $name is not able to fetch item", e)
        }
        finally {
            int remainingCount = inProgressCounter.decrementAndGet()
            LOGGER.info("buffer: $name fetched an item, $remainingCount remaining")
        }
    }
}
