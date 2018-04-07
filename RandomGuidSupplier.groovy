import org.springframework.stereotype.Component

import java.util.function.Supplier

/**
 * @author Jonatan Ivanov
 */
@Component
class RandomGuidSupplier implements Supplier<String> {
    @Override
    String get() {
        Thread.sleep((int)(Math.random() * 5_000) + 2_000)
        return UUID.randomUUID().toString()
    }
}
