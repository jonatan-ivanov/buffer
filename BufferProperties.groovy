import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author Jonatan Ivanov
 */
@Component
@ConfigurationProperties('buffers')
class BufferProperties {
    List<BufferProperty> bufferList
}

class BufferProperty {
    String name
    String supplierName
    int desiredSize
}
