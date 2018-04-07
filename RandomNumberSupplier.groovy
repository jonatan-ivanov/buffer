import org.springframework.stereotype.Component

import java.util.function.Supplier

/**
 * @author Jonatan Ivanov
 */
@Component
class RandomNumberSupplier implements Supplier<Integer> {
    @Override
    Integer get() {
        Thread.sleep((int)(Math.random() * 5_000) + 2_000)
        return (int)(Math.random() * 10)
    }
}
