import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

import java.util.function.Supplier

/**
 * @author Jonatan Ivanov
 */
@Component
class BufferFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext

    Buffer createBuffer(BufferProperty bufferProperty) {
        return new Buffer(
                bufferProperty.name ?: UUID.randomUUID().toString(),
                applicationContext.getBean(bufferProperty.supplierName, Supplier.class),
                bufferProperty.desiredSize
        )
    }

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
    }
}
